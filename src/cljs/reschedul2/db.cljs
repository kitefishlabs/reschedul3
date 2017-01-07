(ns reschedul2.db)

(def default-db
  { :page :home
    :default-user
      { :_id "5861674414c82274d94c7fd5"
        :username "tms"
        :pass "xxx"
        :first_name "Tom"
        :last_name "Stoll"
        :admin true
        :role "admin"
        :contact-info
          { :cell-phone "999-9999"
            :second-phone "888-7777"
            :email "tms@kitefishlabs.com"
            :address ""
            :preferred_contact_method "email"}}})
