(ns sand-compass.views.menusnavs
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [ajax.core :refer [GET POST]]
            [sand-compass.ajax :refer [load-interceptors!]]
            [sand-compass.handlers]
            [sand-compass.subscriptions]))
  ; (:import goog.History))

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
      {:href (str "#/" href)}
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
                    [:p "This is just a test..."]]]
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
      [:span.logo-mini [:b "SC"]]         ; mini logo for sidebar mini 50x50 pixels
      [:span.logo-lg [:b "Sand Compass"]]]   ; logo for regular state and mobile devices
    ; [main-navbar]
    ])

(defn main-sidebar []
  [:aside.main-sidebar

    [:section.sidebar
      [:div.user-panel
        [:div.pull-left.image
          [user-image "img/user2-160x160.jpg" "inherit"]]
        [:div.pull-left.info
          [:p "J. Doe"]
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
            [treeview-menu-item "users" "Configuration"]]]

        [:li.treeview
          [:a { :href "#"}
            [:i.fa.fa-files-o]
            [:span "Communicate"]
            [:span.pull-right-container
              [:i.fa.fa-angle-left.pull-right]]]

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

(defn main-footer []
  [:footer.main-footer
    [:div.pull-right.hidden-xs
      "Sand Compass"]
    [:strong
      "Copyright 2018 "
      [:a
        {:href "about"}
        "by Tom Stoll."]]])
