(ns jarvis.apps.projects.documents.new-document
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [jarvis.apps.projects.documents.common-ui :refer [document-ui]]))


(defn new-document-ui []
  (let [user (rf/subscribe [:identity])
        project (rf/subscribe [:projects/active])]

    (set! (.-title js/document) "New Document")

    (fn []
      (when (and @project @user)
        (let [document (r/atom {:project-id (:id @project)
                                :user-id (:id @user)})]
          [document-ui document])))))
