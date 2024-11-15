package by.andd3dfx.specs

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
        and: 'prev & next link matched to expected values'
        assert getResponse.responseData.prev == 'eyJmIjpmYWxzZSwiaSI6MywibiI6bnVsbCwidiI6bnVsbCwibyI6IkFTQyJ9'
        assert getResponse.responseData.next == 'eyJmIjp0cnVlLCJpIjo0LCJuIjpudWxsLCJ2IjpudWxsLCJvIjoiQVNDIn0='
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
}
