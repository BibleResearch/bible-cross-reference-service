(ns server
  (:require [io.pedestal.http :as http]
            [ns-tracker.core :as nst]
            [service :refer [routes]]))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/host   "0.0.0.0"
   ::http/port   3000})

(defn start []
  (http/start (http/create-server service-map)))

(defonce server (atom nil))

(defn stop-dev []
  (http/stop @server))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn restart-dev []
  (stop-dev)
  (start-dev))

(defn- check-namespace-changes [tracker]
  (try
    (doseq [ns-sym (tracker)]
      (require ns-sym :reload)
      (restart-dev)
      (println (str "Reloaded " ns-sym)))
    (catch Throwable e (.printStackTrace e)))
  (Thread/sleep 100))

(defn start-nstracker []
  (let [tracker (nst/ns-tracker ["src" "resources"])]
    (doto
     (Thread.
      #(while true
         (check-namespace-changes tracker)))
      (.setDaemon true)
      (.start))))
