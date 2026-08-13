package by.andd3dfx.specs

import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseException
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification

import static by.andd3dfx.configs.Configuration.restClient

class SomeSpec extends Specification {

    def 'Read articles using cursor'() {
        when: 'get articles using cursor'
        def getResponse = restClient.get(
                path: '/articles',
                query: [size: 2, cursor: 'eyJmIjp0cnVlLCJpIjoyLCJuIjpudWxsLCJ2IjpudWxsLCJvIjoiQVNDIn0'],
        )

        then: 'server returns 200 code (ok)'
        assert getResponse.status == 200
        and: 'got 2 records'
        assert getResponse.responseData.data.size() == 2
        and: 'prev cursor points to id 3, backward, ASC'
        def prev = decodeCursor(getResponse.responseData.prev as String)
        assert prev.i == 3
        assert prev.o == 'ASC'
        assert prev.f == false
        and: 'next cursor points to id 4, forward, ASC'
        def next = decodeCursor(getResponse.responseData.next as String)
        assert next.i == 4
        assert next.o == 'ASC'
        assert next.f == true
        and: 'cursor payload has no duplicated forward key'
        assert !prev.containsKey('forward')
        assert !next.containsKey('forward')
    }

    def 'First page has no prev cursor'() {
        when: 'get first page without cursor'
        def getResponse = restClient.get(
                path: '/articles',
                query: [size: 2],
        )

        then: 'server returns 200 code (ok)'
        assert getResponse.status == 200
        and: 'got 2 records'
        assert getResponse.responseData.data.size() == 2
        and: 'prev is absent on the first page'
        assert getResponse.responseData.prev == null
        and: 'next is present'
        assert getResponse.responseData.next != null
    }

    def 'Read articles with sort_by and sort_order'() {
        when: 'get first page sorted by author'
        def getResponse = restClient.get(
                path: '/articles',
                query: [size: 2, sort_by: 'author', sort_order: 'ASC'],
        )

        then: 'server returns 200 code (ok)'
        assert getResponse.status == 200
        and: 'got 2 records'
        assert getResponse.responseData.data.size() == 2
        and: 'prev is absent on the first page'
        assert getResponse.responseData.prev == null
        and: 'next cursor stores sort field'
        def next = decodeCursor(getResponse.responseData.next as String)
        assert next.n == 'author'
        assert next.o == 'ASC'
        assert next.f == true
    }

    def 'Reject sort_by together with cursor'() {
        when: 'pass sort_by and cursor together'
        restClient.get(
                path: '/articles',
                query: [
                        size   : 2,
                        sort_by: 'title',
                        cursor : 'eyJmIjp0cnVlLCJpIjoyLCJuIjpudWxsLCJ2IjpudWxsLCJvIjoiQVNDIn0'
                ],
        )

        then: 'server returns 400'
        HttpResponseException e = thrown()
        assert e.statusCode == 400
        and: 'error explains the mistake'
        def problem = new JsonSlurper().parse(e.response.responseData)
        assert problem.detail.contains("Do not pass query parameter 'sort_by' together with 'cursor'")
    }

    def 'Going back to first page clears prev'() {
        given: 'first page'
        def firstPage = restClient.get(path: '/articles', query: [size: 2])
        def nextCursor = firstPage.responseData.next as String

        when: 'go forward'
        def secondPage = restClient.get(path: '/articles', query: [size: 2, cursor: nextCursor])
        def prevCursor = secondPage.responseData.prev as String

        then: 'second page has both cursors'
        assert secondPage.status == 200
        assert prevCursor != null
        assert secondPage.responseData.next != null

        when: 'go back to the first page'
        def backToFirst = restClient.get(path: '/articles', query: [size: 2, cursor: prevCursor])

        then: 'prev is absent again'
        assert backToFirst.status == 200
        assert backToFirst.responseData.data.size() == 2
        assert backToFirst.responseData.prev == null
        assert backToFirst.responseData.next != null
    }

    def 'Read particular article'() {
        when: 'get particular article'
        def getResponse = restClient.get(path: '/articles/1')

        then: 'server returns 200 code (ok)'
        assert getResponse.status == 200
        and: 'got expected article'
        assert getResponse.responseData.title == 'Игрок'
        assert getResponse.responseData.summary == 'Рассказ о страсти игромании'
        assert getResponse.responseData.author == 'Федор Достоевский'
    }

    def 'Create an article'() {
        when: 'create an article'
        def createResponse = restClient.post(
                path: '/articles',
                body: [title: 'Some new title', summary: 'Bla-bla summary', text: 'BomBiBom', author: 'Pushkin'],
                requestContentType: 'application/json'
        )

        then: 'got 201 status'
        assert createResponse.status == 201
        and: 'got created article details in response'
        assert createResponse.responseData.title == 'Some new title'
        assert createResponse.responseData.summary == 'Bla-bla summary'
        assert createResponse.responseData.text == 'BomBiBom'
        assert createResponse.responseData.author == 'Pushkin'

        cleanup:
        restClient.delete(path: '/articles/' + createResponse.responseData.id)
    }

    def 'Delete an article'() {
        setup: 'create an article'
        def createResponse = restClient.post(
                path: '/articles',
                body: [
                        title  : generateRandomString(10),
                        summary: generateRandomString(10),
                        text   : 'Weird text',
                        author : 'Goncharov'
                ],
                requestContentType: 'application/json'
        )
        and: 'got 201 status'
        assert createResponse.status == 201
        def id = createResponse.responseData.id

        when: 'delete particular article'
        def deleteResponse = restClient.delete(path: '/articles/' + id)

        then: 'server returns 204 code'
        assert deleteResponse.status == 204

        and: 'try to get deleted article by id'
        try {
            restClient.get(path: '/articles/' + id)
            throw new RuntimeException("Should not found an article")
        } catch (HttpResponseException hre) {
            and: 'got an 404 error'
            assert hre.statusCode == 404
        }
    }

    def 'Update an article'() {
        when:
        def newTitle = generateRandomString(10)
        def updateResponse = restClient.patch(
                path: '/articles/2',
                body: [title: newTitle],
                requestContentType: 'application/json'
        )

        then: 'got 200 status'
        assert updateResponse.status == 200

        and: 'read an article'
        def getResponse = restClient.get(path: '/articles/2')
        and: 'got 200 status'
        assert getResponse.status == 200
        assert getResponse.responseData.title == newTitle
    }

    String generateRandomString(int count) {
        RandomStringUtils.random(count, true, true)
    }

    private static Map decodeCursor(String encoded) {
        def json = new String(encoded.decodeBase64(), 'UTF-8')
        (Map) new JsonSlurper().parseText(json)
    }
}
