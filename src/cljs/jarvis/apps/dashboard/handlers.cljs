(ns jarvis.apps.dashboard.handlers
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]
    [jarvis.env :as env]
    [jarvis.utils.events :refer [base-interceptors query]]))

;;; ---------------------------------------------------------------------------
;;; HANDLERS
;;; ---------------------------------------------------------------------------


(rf/reg-event-db
  :jarvis.chat/add-message
  base-interceptors
  (fn [db [msg]]
    (update db :jarvis/chat conj 
      (assoc msg :timestamp (str (js/Date.))))))


(rf/reg-event-fx
  :openai/generate-answer-success
  base-interceptors
  (fn [_ [^js response]]
    (let [output-msg (-> response :choices first :message)]
   	  (println output-msg)
   	  {:dispatch-n [[:assoc-in [:jarvis.chat/status] :idle]
                    [:jarvis.chat/add-message output-msg]]})))


(rf/reg-event-fx
  :openai/generate-answer
  base-interceptors
  (fn [_ [user-input]]
    (let [msg {:role "user" 
               :content user-input}]
   	  {:dispatch-n [[:jarvis.chat/add-message msg]
                    [:assoc-in [:jarvis.chat/status] :pending]]
       :http-xhrio {:method :post
   	                :uri "https://api.openai.com/v1/chat/completions"
   	                :headers {"Authorization" (str "Bearer " env/openai-api-key)}
                    :format          (ajax/json-request-format)
                   	:response-format (ajax/json-response-format {:keywords? true})
   	                :params {"model" "gpt-3.5-turbo"
   	                         "messages" [msg]
   	                         "temperature" 1.0}
   	                :on-success [:openai/generate-answer-success]
   	                :on-failure [:common/log]}})))



;;; ---------------------------------------------------------------------------
;;; SUBSCRIPTIONS
;;; ---------------------------------------------------------------------------

(rf/reg-sub :jarvis/chat query)
(rf/reg-sub :jarvis.chat/status query)