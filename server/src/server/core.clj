(ns server.core
    (:require [org.httpkit.server :as server]
      [compojure.core :refer :all]
      [compojure.route :as route]
      [ring.middleware.defaults :refer :all]
      [clojure.pprint :as pp]
      [clojure.string :as str]
      [clojure.data.json :as json]
      [clj-time.core :as t]
      [overtone.at-at :as at-at])
    (:gen-class))

(def colours {:red "#AF4545"
              :orange "#E37737"
              :yellow "#c0BA3E"
              :green "#49C849"
              :cyan "#4AC1BD"
              :blue "#4470D5"})

(def sefkhet-abwy (atom {}))

(def hermes-trismegistus (atom [1 100]))

(defn acknowledge-guest [name]
      (swap! sefkhet-abwy assoc name (t/now)))

(defn who-goes-there? [req]
      (if-let [ips (get-in req [:headers "x-forwarded-for"])]
              (-> ips (clojure.string/split #",") first)
              (:remote-addr req)))

(defn paint [score]
      (cond
        (> score 95) (:blue colours)
        (> score 90) (:cyan colours)
        (> score 40) (:green colours)
        (> score 10) (:yellow colours)
        (> score 5) (:orange colours)
        :default (:red colours)))

(defn manifest [number]
      (if (>= (count @hermes-trismegistus) (count @sefkhet-abwy))
        (swap! hermes-trismegistus #(->> (next %) (into []))))
      (swap! hermes-trismegistus conj number)
      (let [max-num (apply max @hermes-trismegistus)
            min-num (apply min @hermes-trismegistus)
            diff (- max-num min-num)
            score (- 100 diff)]
           (paint score)))


(defn manifestation [req]
      (acknowledge-guest (who-goes-there? req))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (-> (:params req)
                 (:number)
                 (Integer/parseInt)
                 (min 100)
                 (max 1)
                 manifest)})

(defroutes app-routes
           (GET "/" [] "test!")
           (GET "/manifestation" [] manifestation)
           (route/not-found "Error, page not found!"))

(defn -main [& args]
      (let [interval (* 1000 60 5)]
           (at-at/every interval #(let [present (t/now)
                                        past (t/minus present interval)]
                                       (swap! sefkhet-abwy filter (fn [[k v]] (t/before? v past))))
                        (at-at/mk-pool)))

      (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
           (server/run-server (wrap-defaults #'app-routes site-defaults)
                              {:port port})
           (println (str "Running webserver at http://127.0.0.1:" port "/"))))
