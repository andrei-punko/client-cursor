# Presentation docs

Слайды: [cursor-api-presentation.md](cursor-api-presentation.md)  
Тема: [cursor-api-theme.css](cursor-api-theme.css)  

## Install Marp CLI (Chocolatey)

1. Install [Chocolatey](https://chocolatey.org/install) if needed (PowerShell as Administrator).
2. Install Marp CLI:

```bat
choco install marp-cli
```

3. Verify:

```bat
marp --version
```

Пакет community-maintained: https://community.chocolatey.org/packages/marp-cli

## Build PPTX

Из папки `docs`:

```bat
build-pptx.bat
```

Или вручную:

```bat
marp --theme-set cursor-api-theme.css --pptx cursor-api-presentation.md
```

Устный текст лежит в HTML-комментариях слайдов и попадает в Notes при экспорте в PPTX.

> Note: Marp `--pptx` рендерит слайды как изображения. Текст на слайдах в PowerPoint не редактируется; править нужно `.md` и пересобирать. Notes при этом доступны.
