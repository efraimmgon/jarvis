(ns jarvis.apps.projects.core
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [jarvis.utils.views :as views]
   [jarvis.apps.projects.documents :as documents]
   [jarvis.apps.projects.new-project :refer [new-project-ui]]
   [jarvis.apps.projects.edit-project :refer [edit-project-ui]]
   jarvis.apps.projects.handlers
   [jarvis.utils.input :as input]))



;;; ---------------------------------------------------------------------------
;;; Views
;;; ---------------------------------------------------------------------------

(defn projects-header []
  [:div
   {:class "row mb-4 mb-md-0"}

   [:div
    {:class "col-md-8 me-auto my-auto text-left"}
    [:h5 "Projects"]]

   [:div
    {:class "col-lg-4 col-md-12 my-auto text-end"}
    [:a
     {:href (rfe/href :projects/new)
      :class "btn bg-gradient-primary mb-0 mt-0 mt-md-n9 mt-lg-0"}
     [:i
      {:class "material-icons text-white position-relative text-md pe-2"}
      "add"]
     "Add New"]]])

(defn linkfy [href comp]
  [:a {:href href} comp])

(defn project-ui [project]
  [:div {:class "col-lg-4 col-md-6 mb-4"}
   [linkfy (rfe/href :projects/edit {:project-id (:id project)})
    [:div
     {:class "card"}
     [:div
      {:class "card-body p-3"}
      [:div
       {:class "d-flex mt-n2"}

          ;; PROJECT LOGO
       (if-let [logo (:logo project)]
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

          ;; PROJECT HEADER
       [:div
        {:class "ms-3 my-auto"}

           ;; PROJECT NAME
        [:h6 {:class "mb-0"} (:name project)]

           ;; AVATAR LIST (USERS INVOLVED)
        #_(when-let [participants (seq (:participants project))]
            [:div
             {:class "avatar-group"}
             (for [user participants]
               ^{:key (:id user)}
               [:a
                {:href "javascript:;",
                 :class "avatar avatar-xs rounded-circle",
                 :data-bs-toggle "tooltip",
                 :data-original-title (:username user)}])])]


                ;; DROPDOWN ACTIONS
       #_[:div
          {:class "ms-auto"}
          [:div
           {:class "dropdown"}
           [:button
            {:class "btn btn-link text-secondary ps-0 pe-2",
             :id "navbarDropdownMenuLink",
             :data-bs-toggle "dropdown",
             :aria-haspopup "true",
             :aria-expanded "false"}
            [:i {:class "fa fa-ellipsis-v text-lg"}]]
           [:div
            {:class "dropdown-menu dropdown-menu-end me-sm-n4 me-n3",
             :aria-labelledby "navbarDropdownMenuLink"}
            [:a {:class "dropdown-item", :href "javascript:;"} "Action"]
            [:a
             {:class "dropdown-item", :href "javascript:;"}
             "Another action"]
            [:a
             {:class "dropdown-item", :href "javascript:;"}
             "Something else here"]]]]]


      ;; PROJECT DESCRIPTION 

      [:p
       {:class "text-sm mt-3"
        :dangerouslySetInnerHTML
        {:__html (-> project :description clj->js input/editorjs-parser)}}]


      [:hr {:class "horizontal dark"}]

         ;; PROJECT FOOTER
      [:div
       {:class "row"}

          ;; PARTICIPANTS
       (when-let [participants (seq (:participants project))]
         [:div
          {:class "col-6"}
          [:h6 {:class "text-sm mb-0"} (count participants)]
          [:p
           {:class "text-secondary text-sm font-weight-normal mb-0"}
           "Participants"]])

          ;; DUE DATE
       (when-let [due-date (:due-date project)]
         [:div
          {:class "col-6 text-end"}
          [:h6 {:class "text-sm mb-0"} due-date]
          [:p
           {:class "text-secondary text-sm font-weight-normal mb-0"}
           "Due date"]])]]]]])


(defn list-projects []
  (r/with-let [projects (rf/subscribe [:projects/all])]
    [:div
     {:class "row mt-lg-4 mt-2"}
     (if (empty? @projects)
       [:p "No projects yet."]
       (doall
        (for [project @projects]
          ^{:key (:id project)}
          [project-ui project])))]))



(defn all-projects-ui []
  [views/dashboard-base-ui
   [:div
    {:class "container-fluid py-4"}
    [:section
     {:class "py-3"}

     [projects-header]

     [list-projects]]]])



;;; ---------------------------------------------------------------------------
;;; Router

(def router
  ["/projects"

   ["/new"
    {:name :projects/new
     :view #'new-project-ui}]

   ["/{project-id}"
    {:parameters {:path {:project-id string?}}
     :controllers [{:parameters {:path [:project-id]}
                    :start (fn [path]
                             (rf/dispatch
                              [:projects/load-by-id
                               (get-in path [:path :project-id])]))
                    :stop (fn [_]
                            (rf/dispatch
                             [:projects/set-active nil]))}]}

    ["/edit"
     {:name :projects/edit
      :view #'edit-project-ui}]]])
   
   


