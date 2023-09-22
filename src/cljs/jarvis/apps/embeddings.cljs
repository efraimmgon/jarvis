(ns jarvis.apps.embeddings
  (:require
   [ajax.core :as ajax]
   [re-frame.core :as rf]
   [jarvis.env :as env]
   [jarvis.utils.events :refer [base-interceptors]]
   [jarvis.utils.fsdb :as fsdb]))

;; - create text embedding: 
;; 	- take a text and create an embedding for it; 
;; 	- assocs the embedding onto the original text map
;; 	- saves the embedding in a embeddings dir

(defn get-embedding [response]
  (-> response :data first :embedding))

(defn get-tokens [response]
  (-> response :embedding :usage :total_tokens))


; save embeddings into the project given
; schema:
; projects
;   project-id
;     embeddings
;       embedding-id
;         embedding

(rf/reg-event-fx
 :write-embeddings
 base-interceptors
 (fn [_ [coll response]]
   (println "Tokens:" (get-tokens response))
   (let [response (get-embedding response)]
     {:dispatch-n
      [[:fsdb/query
        (fsdb/create!
         {:coll :embeddings,
          :data response})]
       [:fsdb/query
        (fsdb/create!
         {:coll coll,
          :data (get-tokens response)})]]})))


; max 8191 tokens
(rf/reg-event-fx
 :create-embedding
 base-interceptors
 (fn [_ [{:keys [input on-success on-failure]}]]
   {:http-xhrio {:uri "https://api.openai.com/v1/embeddings"
                 :method :post
                 :headers {"Authorization" (str "Bearer " env/openai-api-key)}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :params {:input input
                          :model "text-embedding-ada-002"}
                 :on-success on-success
                 :on-failure (or on-failure [:common/log])}}))


(defn create-embedding!
  "Create an embedding using open ai api."
  [input]
  (js/Promise.
   (fn [resolve reject]
     (ajax/ajax-request
      {:method :post
       :uri "https://api.openai.com/v1/embeddings"
       :headers {"Authorization" (str "Bearer " env/openai-api-key)}
       :params {:input input
                :model "text-embedding-ada-002"}
       :response-format (ajax/json-response-format {:keywords? true})
       :format (ajax/json-request-format)
       :handler resolve
       :error-handler reject}))))

