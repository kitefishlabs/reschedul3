(ns reschedul2.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [reschedul2.ajax :refer [load-interceptors!]]
            [reschedul2.handlers]
            [reschedul2.subscriptions])
  (:import goog.History))

; (defn nav-link [uri title page collapsed?]
;   (let [selected-page (rf/subscribe [:page])]
;     [:li.nav-item
;      {:class (when (= page @selected-page) "active")}
;      [:a.nav-link
;       {:href uri
;        :on-click #(reset! collapsed? true)} title]]))

(defn user-image
  [img-url width-css]
  [:img.img-circle
    { :src img-url
      :alt "User Image"
      :width width-css}])

(defn treeview-menu-item
  [href value]
  [:li
    [:a
      {:href href}
      [:i.fa.fa-circle-o]
      value]])

(defn main-navbar []
  [:nav.navbar.navbar-static-top
    {:role "navigation"}

    [:a.sidebar-toggle
      {:href "#"
       :data-toggle "offcanvas"
       :role "button"}
      [:span.sr-only "Toggle navigation"]]

    ; <!-- Navbar Right Menu -->
    [:div.navbar-custom-menu
      [:ul.nav.navbar-nav
        [:li.dropdown.messages-menu
          [:a.dropdown-toggle
            {:href "#"
             :data-toggle "dropdown"}
            [:i.fa.fa-envelope-o]
            [:span.label.label-success "4"]]

          [:ul.dropdown-menu
            [:li.header "You have 4 messages"]
            ; <!-- inner menu: contains the messages -->
            [:li
              [:ul.menu
                [:li
                  [:a {:href "#"}
                    [:div.pull-left
                      [user-image "img/user2-160x160.jpg" "inherit"]]
                    [:h4;
                      "Support Team"
                      [:small
                        [:i.fa.fa-clock-o]
                        " 5 mins"]]
                    [:p "THE MESSAGE."]]]
                [:li.footer
                  [:a
                    {:href "#"}
                    "See All Messages"]]]]]]
        [:li.dropdown.notifications-menu
          ; <!-- Menu toggle button -->
          [:a.dropdown-toggle
            { :href "#"
              :data-toggle "dropdown"}
            [:i.fa.fa-bell-o]
            [:span.label.label-warning "10"]]
          ; <!-- Dropdown Menu -->
          [:ul.dropdown-menu
            [:li.header
              "You have 10 notifications"]
            [:li
              ; <!-- Inner Menu: contains the notifications -->
              [:ul.menu
                 [:li ; <!-- start notification -->
                  [:a { :href "#"}
                    [:i.fa.fa-users.text-aqua]
                    "5 new members joined today"]]]]
            ; <!-- end notification -->]
            [:li.footer
              [:a { :href "#" } "View all"]]]]

          ; Skip Tasks Menu, other menus, add as needed

          ; <!-- User Account Menu -->
        [:li.dropdown.user.user-menu
          ; <!-- Menu Toggle Button -->
          [:a.dropdown-toggle
            { :href "#"
              :data-toggle "dropdown"}
              ; <!-- The user image in the navbar-->
            [user-image "img/user2-160x160.jpg" "64px"]
            ; <!-- hidden-xs hides the username on small devices so only the image appears. -->
            [:span.hidden-xs "I. N. Fring-Freeley"]]


          [:ul.dropdown-menu
            ; <!-- The user image in the menu -->
            [:li.user-header
              [user-image "img/user2-160x160.jpg" "64px"]
              [:p "I. N. Fring-Freeley - Barriere"
                [:small "Meta-organizer since Nov. 2005(ish)"]]]

            ;<!-- Menu Body -->
            [:li.user-body
              [:div.row
                [:div.col-xs-4.text-center
                  [:a { :href "#"}
                    "Info"]]
                [:div.col-xs-4.text-center
                  [:a { :href "#"}
                    "Proposals"]]
                [:div.col-xs-4.text-center
                  [:a { :href "#"}
                    "Contact"]]]]

            ;<!-- Menu Footer -->
            [:li.user-footer

              [:div.pull-left
                [:a.btn.btn-xs.btn-default.btn-flat
                  { :href "#"}
                  "Profile"]]
              [:div.pull-right
                [:a.btn.btn-xs.btn-default.btn-flat
                  { :href "#"}
                  "Sign Out"]]]]]]]])



(defn main-header []
  [:header.main-header
    [:a.logo
      { :href "#"}
      [:span.logo-mini [:b "A"] "LT"]         ; mini logo for sidebar mini 50x50 pixels
      [:span.logo-lg [:b "Admin"] "LTE"]]   ; logo for regular state and mobile devices
    [main-navbar]])

(defn main-sidebar []
  [:aside.main-sidebar

    [:section.sidebar

      [:div.user-panel
        [:div.pull-left.image
          [user-image "img/user2-160x160.jpg" "inherit"]]
        [:div.pull-left.info
          [:p "I.N.Fring-Freeley"]
          [:a { :href "#"}
            [:i.fa.fa-circle.text-success]
            "Online"]]]

      [:form.sidebar-form
        { :action "#"
          :method "get"}
        [:div.input-group
          [:input
            { :type "text"
              :name "q"
              :placeholder "Search..."}]
          [:span.input-group-btn
            [:button.btn.btn-flat
              { :type "submit"
                :name "search"
                :id "search-btn"}
              [:i.fa.fa-search]]]]]

      ; <!-- Sidebar Menu -->
      [:ul.sidebar-menu

        [:li.header "MAIN NAVIGATION"]

        ; <!-- Optionally, you can add icons to the links -->
        [:li.active.treeview

          [:a
            { :href "#"}
            [:i.fa.fa-dashboard]
            [:span "Scheduling Dashboard"]
            [:span.pull-right-container
              [:i.fa.fa-angle-left.pull-right]]]

          [:ul.treeview-menu
            [treeview-menu-item "users" "Users"]
            [treeview-menu-item "venues" "Venues"]
            [treeview-menu-item "proposals" "Proposals"]
            [treeview-menu-item "shows" "Shows"]]]

        [:li.treeview
          [:a { :href "#"}
            [:i.fa.fa-files-o]
            [:span "Communicate"]
            [:span.pull-right-container
              [:span.label.label-primary.pull-right "4"]]]

          [:ul.treeview-menu
            [treeview-menu-item
              "#/email-all-orgs"
              "Email all organizers."]
            [treeview-menu-item
              "#/email-devs"
              "Email developers."]
            [treeview-menu-item
              "#/suggest-a-slack"
              "Suggest a Slack channel."]
            [treeview-menu-item
              "#/email-anonymously"
              "Share some intel."]]]]]])


(defn footer []
  [:footer.main-footer
    [:div.pull-right.hidden-xs
      "BiF 2017"]
    [:strong
      "Copyright 2016 "
      [:a
        {:href "about#contributors"}
        "by the authors."]]])

(defn about-page []
  [:div.content-wrapper
    [:section.content-header
      [:h1 "reschedul2..."
        [:small "work in progress"]]
      [:ol.breadcrumb
        [:li
          [:a {:href "#"}
            [:i.fa.fa-dashboard]
            "Level"]]
        [:li.active "Here"]]]])

(defn home-page []
  [:div.content-wrapper
    [:section.content-header
      [:ol.breadcrumb
        [:li
          [:a {:href "#"}
            [:i.fa.fa-dashboard]
            "Level"]]
        [:li.active "Here"]]
      [:h1 "Home"
        [:small "Optional description"]]]
    [:section.content
      [:p "CONTENT"]]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div
   [main-header]
   [main-sidebar]
   [(pages @(rf/subscribe [:page]))]
   [footer]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  ; (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
