(ns jarvis.apps.dashboard.views
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [jarvis.utils.views :refer [dashboard-base-ui]]
    [jarvis.utils.input :as input]
    jarvis.apps.dashboard.handlers
    [jarvis.audio :as audio]))


(defn card [{:keys [header body footer]}]
  [:div.card.mb-2
   (when header 
     [:div.card-header header])
   (when body
     [:div.card-body body])
   (when footer
     [:div.card-footer footer])])


(defn message-component
  [message]
  [card
   {:body 
    [:div 
     [:p 
      (:role message) " - "
      [:span.muted (:timestamp message)]]
     [:p (-> message :content .trim)]]}])


(defn form-group [label & comps]
  (into
    [:div.input-group.input-group-outline.is-filled
     [:label.form-label label]]
    comps))



(defn chatbox-component
  []
  (r/with-let [doc (r/atom nil)
               chat-status (rf/subscribe [:jarvis.chat/status])
               conversation (rf/subscribe [:jarvis/chat])]
    [:div
     [:div
      
      ;; Chat history
      (for [message @conversation]
        ^{:key (:timestamp message)}
        [message-component message])]
      
     [card
      {:body
       [:div
        
        ;; User input
        (when (= :idle @chat-status)
          [form-group
           "Send a message"
           [input/textarea 
            {:doc doc
             :class "form-control"
             :name :user-input}]])
        
         
        [:hr]
          
        ;; Spinner
        (when (= :pending @chat-status)
          [:div.text-center
           [:i.fa.fa-spinner.fa-spin.fa-2x]])
        
        ;; Submit button
        (when (= :idle @chat-status)
          [:button.btn.btn-primary
           {:on-click (fn [_]
                        (rf/dispatch [:openai/generate-answer (:user-input @doc)])
                        (reset! doc nil))}
           "Send text message"])
        " "
        [audio/audio-ui]]}]]))


(defn chatbot []
  [:div.row
   [:div.col-lg-12
    [card 
     {:header "Welcome to Jarvis"
      :body [chatbox-component]}]]])
    


(defn dashboard-ui []
  [dashboard-base-ui
   [:div
    {:class "container-fluid py-4"}
    [chatbot]]])