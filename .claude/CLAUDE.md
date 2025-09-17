# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a full-stack Todo List application with calendar integration:

- **backend/**: NestJS REST API with Prisma ORM
- **frontend/**: React + TypeScript + Vite application with FullCalendar

## Database

The backend uses Prisma with PostgreSQL. The main entity is `Todo` with fields:
- id (Int, primary key), title (String), completed (Boolean, defaults to false), date (String), memo (String, optional), createdAt (DateTime), updatedAt (DateTime)

## Development Commands

### Backend (NestJS)
```bash
cd backend
pnpm install        # Install dependencies
pnpm run start:dev  # Run development server with hot reload
pnpm run build      # Build for production
pnpm run start:prod # Run production build
pnpm run test       # Run unit tests
pnpm run test:e2e   # Run end-to-end tests
pnpm run test:cov   # Run tests with coverage
pnpm run lint       # Run ESLint
pnpm run format     # Format code with Prettier
```

### Frontend (React + Vite)
```bash
cd frontend
pnpm install    # Install dependencies
pnpm run dev    # Start development server
pnpm run build  # Build for production
pnpm run preview # Preview production build
pnpm run lint   # Run ESLint
```

## Architecture Overview

### Backend Architecture
- **Controller Layer**: `AppController` handles HTTP requests for `/api/todos` endpoints
- **Service Layer**: `AppService` contains business logic
- **Data Layer**: `PrismaService` handles database operations
- **DTOs**: `CreateTodoDto` for request validation using class-validator

### Frontend Architecture
- **Single Page Application**: Main `App.tsx` component manages todo CRUD operations
- **Calendar Integration**: Uses FullCalendar React component for calendar view
- **State Management**: React hooks (useState, useEffect) for local state
- **API Communication**: Direct fetch calls to `/api/todos` endpoints
- **Styling**: Tailwind CSS for UI components

### API Endpoints
- `GET /api/todos` - Fetch all todos
- `POST /api/todos` - Create new todo
- `PATCH /api/todos/:id` - Update existing todo
- `DELETE /api/todos/:id` - Delete todo

## Key Dependencies

### Backend
- NestJS framework with Express
- Prisma ORM with PostgreSQL
- class-validator for DTO validation
- Jest for testing

### Frontend
- React 19 with TypeScript
- Vite for build tooling
- FullCalendar for calendar functionality
- Tailwind CSS for styling

## Development Notes

- The backend expects to connect to PostgreSQL (DATABASE_URL in .env)
- Frontend uses Vite proxy for API calls in development
- Both applications use pnpm as package manager
- TypeScript is used throughout the stack for type safety