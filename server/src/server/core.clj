(ns server.core
    (:require [org.httpkit.server :as server]
      [compojure.core :refer [defroutes GET]]
      [compojure.route :as route]
      [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
      [clojure.pprint :as pp]
      [clojure.string :as str]
      [clojure.data.json :as json]
      [clj-time.core :as t]
      [overtone.at-at :as at-at]
      [ring.middleware.json :refer [wrap-json-response]]
      [ring.middleware.cors :refer [wrap-cors]])
    (:gen-class))

(def sefkhet-abwy (atom {}))

(def hermes-trismegistus (atom [1 100]))

(defn acknowledge-guest [name]
      (swap! sefkhet-abwy assoc name (t/now)))

(defn who-goes-there? [req]
      (if-let [ips (get-in req [:headers "x-forwarded-for"])]
              (-> ips (clojure.string/split #",") first)
              (:remote-addr req)))

(def visible-spectrum {:700–635nm "#AF4545"
              :635–590nm "#E37737"
              :590–560nm  "#C0BA3E"
              :560–520nm "#49C849"
              :520–490nm "#4AC1BD"
              :490–450nm "#4470D5"})

(defn iris [score]
      (cond
        (> score 95) (:490–450nm visible-spectrum)
        (> score 90) (:520–490nm visible-spectrum)
        (> score 40) (:560–520nm visible-spectrum)
        (> score 10) (:590–560nm visible-spectrum)
        (> score 5) (:635–590nm visible-spectrum)
        :default (:700–635nm visible-spectrum)))

(defn manifest [number]
      (if (>= (count @hermes-trismegistus) (count @sefkhet-abwy))
        (swap! hermes-trismegistus #(->> (next %) (into []))))
      (swap! hermes-trismegistus conj number)
      (let [max-num (apply max @hermes-trismegistus)
            min-num (apply min @hermes-trismegistus)
            diff (- max-num min-num)
            score (- 100 diff)]
           (iris score)))


(defn manifestation [req]
      (acknowledge-guest (who-goes-there? req))
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:number (-> (:params req)
                 (:number)
                 (Integer/parseInt)
                 (min 100)
                 (max 1)
                 manifest)}})

(defroutes app-routes
           (GET "/manifestation" [] manifestation)
           (route/not-found "Error, page not found!"))

(defn -main [& args]
      (let [interval (* 1000 60 5)]
           (at-at/every interval #(let [present (t/now)
                                        past (t/minus present interval)]
                                       (swap! sefkhet-abwy filter (fn [[k v]] (t/before? v past))))
                        (at-at/mk-pool)))

      (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
           (server/run-server (-> #'app-routes (wrap-defaults api-defaults) (wrap-json-response) (wrap-cors :access-control-allow-origin [#".*"] :access-control-allow-methods [:get :post]))
                              {:port port})
           (println (str "Running webserver at http://127.0.0.1:" port "/"))))
