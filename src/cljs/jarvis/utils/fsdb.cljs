(ns jarvis.utils.fsdb
  (:require
   [ajax.core :as ajax]
   [re-frame.core :as rf]
   [jarvis.utils.events :refer [base-interceptors]]))


(defn get-by-id
  [{:keys [coll id] :as params}]
  {:fn :get-by-id
   :params params})


(defn get-all
  [{:keys [coll] :as params}]
  {:fn :get-all
   :params params})


(defn select
  [{:keys [coll where order-by offset limit] :as params}]
  {:fn :select
   :params params})


(defn create!
  [{:keys [coll data] :as params}]
  {:fn :create!
   :params params})


(defn create-raw!
  [{:keys [coll data] :as params}]
  {:fn :create-raw!
   :params params})


(defn update!
  [{:keys [coll where data opts] :as params}]
  {:fn :update!
   :params params})


(defn upsert!
  [{:keys [coll where data opts] :as params}]
  {:fn :upsert!
   :params params})


(defn delete!
  [{:keys [coll id] :as params}]
  {:fn :delete!
   :params params})


(rf/reg-event-fx
 :fsdb/query
 base-interceptors
 (fn [_ [{:keys [params on-success on-failure]}]]
   {:http-xhrio {:method :post
                 :uri "/api/fsdb"
                 :params params
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success on-success
                 :on-failure (or on-failure [:common/log])}}))

(defn delete-coll!
  [{:keys [coll] :as params}]
  {:fn :delete-coll!
   :params params})

