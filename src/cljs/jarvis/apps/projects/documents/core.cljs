(ns jarvis.apps.projects.documents.core
  (:require [jarvis.utils.input :as input]
    [jarvis.utils.views :as views]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reitit.frontend.easy :as rfe]
    [jarvis.apps.projects.documents.edit-document :refer [edit-document-ui]]
    [jarvis.apps.projects.documents.new-document :refer [new-document-ui]]
    jarvis.apps.projects.documents.handlers))


;;; ---------------------------------------------------------------------------
;;; Views
;;; ---------------------------------------------------------------------------

(defn document-item-ui [document]
  [:div {:class "col-lg-4 col-md-6 mb-4"}
   [:a {:href (rfe/href :projects/edit {:project-id (:id document)})}
    [:div
     {:class "card"}
     [:div
      {:class "card-body p-3"}
      [:div
       {:class "d-flex mt-n2"}

       ;; LOGO
       (if-let [logo (:logo document)]
         [:div
          {:class
           "avatar avatar-xl bg-gradient-dark border-radius-xl p-2 mt-n4"}
          [:img
           {:src (:src logo),
            :alt (:alt logo)}]]
         [:div
          {:class
           "avatar avatar-xl bg-gradient-dark border-radius-xl p-2 mt-n4"}
          [:img
           {:src "/logo-slack.svg",
            :alt "placeholder"}]])

       ;; HEADER ---
       [:div
        {:class "ms-3 my-auto"}

        ;; NAME
        [:h6 {:class "mb-0"} (:name document)]]]


      ;; DESCRIPTION 

      [:p
       {:class "text-sm mt-3"
        :dangerouslySetInnerHTML
        {:__html (-> document :description clj->js input/editorjs-parser)}}]


      [:hr {:class "horizontal dark"}]

      ;; FOOTER
      [:div
       {:class "row"}

       ;; DUE DATE
       (when-let [due-date (:due-date document)]
         [:div
          {:class "col-6 text-end"}
          [:h6 {:class "text-sm mb-0"} due-date]
          [:p
           {:class "text-secondary text-sm font-weight-normal mb-0"}
           "Due date"]])]]]]])


(defn documents-header [project]
  [:div
   {:class "row mb-4 mb-md-0"}

   [:div
    {:class "col-md-8 me-auto my-auto text-left"}
    [:h5 "Projects"]]

   [:div
    {:class "col-lg-4 col-md-12 my-auto text-end"}
    [:a
     {:href (rfe/href :projects.documents/new {:project-id (:id @project)})
      :class "btn bg-gradient-primary mb-0 mt-0 mt-md-n9 mt-lg-0"}
     [:i
      {:class "material-icons text-white position-relative text-md pe-2"}
      "add"]
     "Add New"]]])


(defn list-documents [documents]
  [:div
   {:class "row mt-lg-4 mt-2"}
   (if (empty? @documents)
     [:p "No documents yet."]
     (doall
       (for [project @documents]
         ^{:key (:id project)}
         [document-item-ui project])))])



(defn all-documents-ui []
  (r/with-let [project (rf/subscribe [:projects/active])
               documents (rf/subscribe [:projects.documents/list])]
    (set! (.-title js/document) (str (:name @project) " - Documents"))
    [views/dashboard-base-ui
     [:div
      {:class "container-fluid py-4"}
      [:section
       {:class "py-3"}

       [documents-header project]

       [list-documents documents]]]]))


;;; ---------------------------------------------------------------------------
;;; Router


(def router
  ["/documents"
   ["/"
    {:name :projects.documents/list
     :view #'all-documents-ui
     :controllers [{:parameters {:path [:project-id]}
                    :start (fn [path]
                             (rf/dispatch
                               [:projects.documents/load
                                (get-in path [:path :project-id])]))
                    :stop (fn [_]
                            (rf/dispatch
                              [:projects.documents/set nil]))}]}]

   ; TODO
   ["/new"
    {:name :projects.documents/new
     :view #'new-document-ui}]

   ; TODO
   ["/:document-id/edit"
    {::parameters {:path {:document-id string?}}
     :name :projects.documents/edit
     :view #'edit-document-ui}]])
