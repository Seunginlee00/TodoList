import { useState, useEffect, useCallback } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin, { DateClickArg } from "@fullcalendar/interaction";
import { EventClickArg, EventInput } from "@fullcalendar/core";
import { apiFetch } from "../utils/api";

interface CalendarEvent {
  id: number;
  title: string;
  start: string;
  end: string;
  allDay: boolean;
}

interface ModalState {
  isOpen: boolean;
  isNew: boolean;
  currentEvent: Partial<CalendarEvent>;
}

export default function CalendarPage() {
  const [events, setEvents] = useState<EventInput[]>([]);
  const [modalState, setModalState] = useState<ModalState>({
    isOpen: false,
    isNew: true,
    currentEvent: {},
  });

  const fetchEvents = useCallback(() => {
    apiFetch("/api/calendar")
      .then((res) => res.json())
      .then((data: CalendarEvent[]) => {
        const formattedEvents = data.map((event) => ({
          id: String(event.id),
          title: event.title,
          start: event.start,
          end: event.end,
          allDay: event.allDay,
          backgroundColor: "#10B981", // Green for calendar events
          borderColor: "#10B981",
        }));
        setEvents(formattedEvents);
      });
  }, []);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const handleDateClick = (arg: DateClickArg) => {
    setModalState({
      isOpen: true,
      isNew: true,
      currentEvent: { start: arg.dateStr, end: arg.dateStr, allDay: true },
    });
  };

  const handleEventClick = (arg: EventClickArg) => {
    const { id, title, start, end, allDay } = arg.event;
    setModalState({
      isOpen: true,
      isNew: false,
      currentEvent: {
        id: Number(id),
        title,
        start: start ? start.toISOString().slice(0, 16) : "", // datetime-local format
        end: end ? end.toISOString().slice(0, 16) : "",
        allDay: allDay,
      },
    });
  };

  const closeModal = () => {
    setModalState({ isOpen: false, isNew: true, currentEvent: {} });
  };

  const handleFormChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value, type } = e.target;
    const checked = (e.target as HTMLInputElement).checked;
    setModalState((prev) => ({
      ...prev,
      currentEvent: {
        ...prev.currentEvent,
        [name]: type === "checkbox" ? checked : value,
      },
    }));
  };

  const handleFormSubmit = () => {
    const { currentEvent, isNew } = modalState;
    const url = isNew ? "/api/calendar" : `/api/calendar/${currentEvent.id}`;
    const method = isNew ? "POST" : "PUT";
    
    apiFetch(url, {
      method,
      body: JSON.stringify(currentEvent),
    }).then(() => {
      closeModal();
      fetchEvents();
    });
  };

  const handleDelete = () => {
    if (window.confirm("정말 삭제하시겠습니까?")) {
      apiFetch(`/api/calendar/${modalState.currentEvent.id}`, {
        method: "DELETE",
      }).then(() => {
        closeModal();
        fetchEvents();
      });
    }
  };

  return (
    <div className="max-w-5xl mx-auto mt-8 p-4 font-sans">
      <div className="mb-4 flex justify-between items-center">
        <h1 className="text-2xl font-bold">캘린더 (일정 관리)</h1>
        <a href="/main" className="text-blue-500 hover:underline">할 일 목록으로 이동</a>
      </div>
      
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
          right: "dayGridMonth,dayGridWeek",
        }}
      />

      {modalState.isOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-end z-50">
          <div className="bg-white p-6 rounded-t-2xl shadow-xl w-full max-w-md animate-slide-up">
            <h2 className="text-2xl font-bold mb-4">
              {modalState.isNew ? "새 일정" : "일정 수정"}
            </h2>
            <div className="space-y-4">
              <input
                type="text"
                name="title"
                placeholder="일정 제목"
                value={modalState.currentEvent.title || ""}
                onChange={handleFormChange}
                className="w-full p-2 border rounded-md"
              />
              <div className="flex gap-2">
                <div className="flex-1">
                    <label className="text-sm text-gray-600">시작</label>
                    <input
                        type="datetime-local"
                        name="start"
                        value={modalState.currentEvent.start || ""}
                        onChange={handleFormChange}
                        className="w-full p-2 border rounded-md"
                    />
                </div>
                <div className="flex-1">
                    <label className="text-sm text-gray-600">종료</label>
                    <input
                        type="datetime-local"
                        name="end"
                        value={modalState.currentEvent.end || ""}
                        onChange={handleFormChange}
                        className="w-full p-2 border rounded-md"
                    />
                </div>
              </div>
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="allDay"
                  name="allDay"
                  checked={modalState.currentEvent.allDay || false}
                  onChange={handleFormChange}
                  className="h-4 w-4"
                />
                <label htmlFor="allDay" className="ml-2 text-gray-700">
                  종일
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
