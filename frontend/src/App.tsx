import { useState, useEffect, useCallback } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin, { DateClickArg } from "@fullcalendar/interaction";
import { EventClickArg, EventInput } from "@fullcalendar/core";
import { apiFetch } from "./utils/api";

// API 응답에 맞는 Todo 타입 정의
interface Todo {
  id: number;
  title: string;
  completed: boolean;
  date: string;
  memo: string | null;
}

// 모달 상태 타입
interface ModalState {
  isOpen: boolean;
  isNew: boolean;
  currentTodo: Partial<Todo>;
}

function App() {
  const [events, setEvents] = useState<EventInput[]>([]);
  const [modalState, setModalState] = useState<ModalState>({
    isOpen: false,
    isNew: true,
    currentTodo: {},
  });

  // 서버에서 모든 할 일을 가져와 FullCalendar 이벤트 형식으로 변환
  const fetchTodos = useCallback(() => {
    apiFetch("/api/todos")
        .then((res) => res.json())
        .then((todos: Todo[]) => {
          const formattedEvents = todos.map((todo) => ({
            id: String(todo.id),
            title: todo.title,
            start: todo.date, // ✅ FullCalendar는 start 사용
            extendedProps: { memo: todo.memo, completed: todo.completed },
            backgroundColor: todo.completed ? "#6B7280" : "#3B82F6",
            borderColor: todo.completed ? "#6B7280" : "#3B82F6",
          }));
          setEvents(formattedEvents);
        });
  }, []);

  useEffect(() => {
    fetchTodos();
  }, [fetchTodos]);

  // 날짜 클릭 시 모달을 여는 핸들러
  const handleDateClick = (arg: DateClickArg) => {
    setModalState({
      isOpen: true,
      isNew: true,
      currentTodo: { date: arg.dateStr, completed: false },
    });
  };

  // 기존 이벤트 클릭 핸들러 (수정/삭제용)
  const handleEventClick = (arg: EventClickArg) => {
    const { id, title, start, extendedProps } = arg.event; // ✅ date → start
    setModalState({
      isOpen: true,
      isNew: false,
      currentTodo: {
        id: Number(id),
        title,
        date: start ? start.toISOString().split("T")[0] : "",
        memo: extendedProps.memo,
        completed: extendedProps.completed,
      },
    });
  };

  // 모달 닫기
  const closeModal = () => {
    setModalState({ isOpen: false, isNew: true, currentTodo: {} });
  };

  // 폼 입력 변경 핸들러
  const handleFormChange = (
      e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setModalState((prev) => ({
      ...prev,
      currentTodo: {
        ...prev.currentTodo,
        [name]: type === "checkbox" ? checked : value,
      },
    }));
  };

  // 폼 제출 (저장/수정) 핸들러
  const handleFormSubmit = () => {
    const { currentTodo, isNew } = modalState;
    const url = isNew ? "/api/todos" : `/api/todos/${currentTodo.id}`;
    const method = isNew ? "POST" : "PATCH";
    const body = {
      title: currentTodo.title,
      date: currentTodo.date,
      memo: currentTodo.memo,
      completed: currentTodo.completed,
    };
    apiFetch(url, {
      method,
      body: JSON.stringify(body),
    }).then(() => {
      closeModal();
      fetchTodos();
    });
  };

  // 삭제 핸들러
  const handleDelete = () => {
    if (window.confirm("정말 이 일정을 삭제하시겠습니까?")) {
      apiFetch(`/api/todos/${modalState.currentTodo.id}`, {
        method: "DELETE",
      }).then(() => {
        closeModal();
        fetchTodos();
      });
    }
  };

  return (
      <div className="max-w-5xl mx-auto mt-8 p-4 font-sans">
        <FullCalendar
            plugins={[dayGridPlugin, interactionPlugin]}
            initialView="dayGridMonth"
            weekends={true}
            events={events}
            dateClick={handleDateClick}
            eventClick={handleEventClick}
            height="auto"
            locale="ko"
            headerToolbar={{
              left: "prev,next today",
              center: "title",
              right: "dayGridMonth,dayGridWeek,dayGridDay",
            }}
        />

        {/* 모달 UI */}
        {modalState.isOpen && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-end z-50">
              <div className="bg-white p-6 rounded-t-2xl shadow-xl w-full max-w-md animate-slide-up">
                <h2 className="text-2xl font-bold mb-4">
                  {modalState.isNew ? "새로운 할 일" : "할 일 수정"}
                </h2>
                <div className="space-y-4">
                  <input
                      type="text"
                      name="title"
                      placeholder="제목"
                      value={modalState.currentTodo.title || ""}
                      onChange={handleFormChange}
                      className="w-full p-2 border rounded-md"
                  />
                  <input
                      type="date"
                      name="date"
                      value={modalState.currentTodo.date || ""}
                      onChange={handleFormChange}
                      className="w-full p-2 border rounded-md"
                  />
                  <textarea
                      name="memo"
                      placeholder="메모"
                      value={modalState.currentTodo.memo || ""}
                      onChange={handleFormChange}
                      className="w-full p-2 border rounded-md"
                      rows={4}
                  />
                  <div className="flex items-center">
                    <input
                        type="checkbox"
                        id="completed"
                        name="completed"
                        checked={modalState.currentTodo.completed || false}
                        onChange={handleFormChange}
                        className="h-4 w-4"
                    />
                    <label htmlFor="completed" className="ml-2 text-gray-700">
                      완료
                    </label>
                  </div>
                </div>
                <div className="flex justify-between mt-6">
                  <div>
                    {!modalState.isNew && (
                        <button
                            onClick={handleDelete}
                            className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded-md"
                        >
                          삭제
                        </button>
                    )}
                  </div>
                  <div>
                    <button
                        onClick={closeModal}
                        className="mr-2 bg-gray-300 hover:bg-gray-400 text-black py-2 px-4 rounded-md"
                    >
                      취소
                    </button>
                    <button
                        onClick={handleFormSubmit}
                        className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded-md"
                    >
                      {modalState.isNew ? "저장" : "수정"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
        )}
      </div>
  );
}

export default App;
