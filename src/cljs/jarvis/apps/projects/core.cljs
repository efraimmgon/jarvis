(ns jarvis.apps.projects.core
  (:require
   [ajax.core :as ajax]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [jarvis.utils.events :refer [base-interceptors query]]
   [jarvis.utils.fsdb :as fsdb]
   [jarvis.utils.views :as views]))


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
    [:button
     {:type "button",
      :class "btn bg-gradient-primary mb-0 mt-0 mt-md-n9 mt-lg-0"}
     [:i
      {:class
       "material-icons text-white position-relative text-md pe-2"}
      "add"]
     "Add New"]]])

(defn linkfy [href comp]
  [:a {:href href} comp])

(defn project-ui [project]
  [:div {:class "col-lg-4 col-md-6 mb-4"}
   [linkfy (str "/projects/" (:id project))
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
      [:p {:class "text-sm mt-3"}
       (:description project)]

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



(def mock-projects
  (r/atom [{:id 1
            :name "Project 1"
            :description "Project 1 description"
            :participants [{:id 1
                            :username "User 1"
                            :profile {:avatar {:src "https://via.placeholder.com/150"
                                               :alt "User 1"}}}
                           {:id 2
                            :username "User 2"
                            :profile {:avatar {:src "https://via.placeholder.com/150"
                                               :alt "User 2"}}}]
            :due-date "2021-10-10"}
           {:id 2
            :name "Project 2"}]))


(defn list-projects []
  (r/with-let [projects (r/atom nil)]
    [:div
     {:class "row mt-lg-4 mt-2"}
     (if (empty? @mock-projects)
       [:p "No projects yet."]
       (doall
        (for [project @mock-projects]
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
;;; Handlers
;;; ---------------------------------------------------------------------------


;;; ---------------------------------------------------------------------------
;;; Response hanlders

(rf/reg-event-fx
 :projects/all-projects-success
 base-interceptors
 (fn [db [projects]]
   (assoc db :projects/all projects)))

(rf/reg-event-fx
 :projects/get-project-by-id-success
 base-interceptors
 (fn [db [project]]
   (assoc db :projects/active project)))

(rf/reg-event-db
 :projects/create-project-success
 base-interceptors
 (fn [db [project]]
   (update db :projects/all conj project)))

(rf/reg-event-db
 :projects/update-project-success
 base-interceptors
 (fn [db [project]]
   (assoc db :projects/active project)))

(rf/reg-event-db
 :projects/delete-project-success
 base-interceptors
 (fn [db [project-id]]
   (update db :projects/all
           #(remove (fn [p]
                      (= (:id p) project-id))
                    %))))

;;; ---------------------------------------------------------------------------
;;; Main handlers


(rf/reg-event-fx
 :projects/all-projects
 base-interceptors
 (fn [_ _]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-all {:coll [:users (:id @user) :projects]})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/all-projects-success]}]})))


(rf/reg-event-fx
 :projects/get-project-by-id
 base-interceptors
 (fn [_ [{:keys [id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/get-by-id {:coll [:users (:id @user) :projects]
                            :id id})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/get-project-by-id-success]}]})))

(rf/reg-event-fx
 :projects/create-project!
 base-interceptors
 (fn [_ [{:keys [data]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/create! {:coll [:users (:id @user) :projects]
                          :data data})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/create-project-success]}]})))


(rf/reg-event-fx
 :projects/update-project!
 base-interceptors
 (fn [_ [params]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/update!
            (assoc (select-keys params [:where :data :opts])
                   :coll [:users (:id @user) :projects]))]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/update-project-success]}]})))


(rf/reg-event-fx
 :projects/delete-project!
 base-interceptors
 (fn [_ [{:keys [id]}]]
   (let [user (rf/subscribe [:identity])
         q (fsdb/delete! {:coll [:users (:id @user) :projects]
                          :id id})]
     {:dispatch
      [:fsdb/query
       {:params q
        :on-success [:projects/delete-project-success]}]})))


;; subscriptions

(rf/reg-sub :projects/all query)
(rf/reg-sub :projects/active query)