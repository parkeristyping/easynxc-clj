(ns easynxc.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [easynxc.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:rel "stylesheet" :href "http://yui.yahooapis.com/pure/0.6.0/pure-min.css"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def loading-page
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")]))

(def env-page
  (html5
   (head)
   [:body {:class "body-container"}
    [:p (:dev env)]]))

(defn auto-start [song]
  (html5
   (head)
   [:body {:class "body-container"}
    [:div [:h1 song]]
    (include-js "/js/app.js")]))

(defroutes routes
  (GET "/" [] loading-page)
  (GET "/env" [] env-page)
  (GET "/auto-play/:song" [song] loading-page)
  ;; (GET "/:song" [song] (auto-start song))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
