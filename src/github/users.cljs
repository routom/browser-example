(ns github.users)


(def current-user-request
  {:url "https://api.github.com/user"
   :method "GET"
   :headers {"Accept" "application/json"}}
  )