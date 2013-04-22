
(defn post-to-alerta
  "POST to the Alerta REST API."
  [request]
  (let [event-url "http://monitoring.gudev.gnl/alerta/api/v2/alerts/alert.json"]
  	(client/post event-url
               {:body (json/generate-string request)
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true})))

(defn format-alerta-event
  "Formats an event for Alerta."
  [event]
  {
   :origin "riemann"
   :resource (:host event)
   :event (:service event)
   :group "Performance"      ; parse from metric name or tags?
   :value (:metric event)
   :severity (:state event)
   :environment 
   	[(if-let [env-tag (first (filter #(re-matches #"^environment:.*" %) (:tags event)))]
   		(last (clojure.string/split env-tag #":"))
   		"INFRA"
   	)]
   :service
    [(if-let [env-tag (first (filter #(re-matches #"^service:.*" %) (:tags event)))]
   		(last (clojure.string/split env-tag #":"))
   		"Common"
   	)]
   :tags (:tags event)
   :text (:description event)
   :rawData event})

(defn alerta
  "Creates an alerta adapter.
    (changed-state (alerta))"
  [e]
  (post-to-alerta (format-alerta-event e)))

(defn post-heartbeat
  "POST to the Alerta REST API."
  [request]
  (let [event-url "http://monitoring.gudev.gnl:80/alerta/api/v2/heartbeats/heartbeat.json"]
  	(client/post event-url
               {:body (json/generate-string request)
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true})))

(defn heartbeat [e] (post-heartbeat
	{
	   :origin (str "riemann/" hostname)
	   :version version
	   :type "Heartbeat"}))