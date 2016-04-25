(ns github.repos)

(defn user-repos-request
  [username page per-page]
  {:url     (str "https://api.github.com/users/" username "/repos?page=" page "&per_page=" per-page)
   :method  "GET"
   :headers {"Accept" "application/json"}}
  )