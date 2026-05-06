# SilverPear frontend (React + Vite)

## Как открыть интерфейс

1. **Вместе с бэкендом (один порт 8080)**  
   Из папки `frontend` выполните:
   ```bash
   npm install
   npm run build
   ```
   Сборка попадёт в `../src/main/resources/static/`. Затем запустите Spring Boot и откройте в браузере:  
   `http://127.0.0.1:8080/` (или `http://localhost:8080/`).

2. **Режим разработки (HMR)**  
   В одном терминале — Spring на `8080`, в другом:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Откройте URL, который выведет Vite (обычно `http://127.0.0.1:5173/`). Запросы к `/api` проксируются на бэкенд.

Если на Windows `npm install` падает с `EPERM` на файлах в `node_modules`, закройте процессы, которые держат папку проекта (IDE, другой `npm run dev`), временно отключите антивирус для каталога проекта или выполните установку из терминала «от имени администратора».

---

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
