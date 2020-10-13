(ns packetfork.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:import (java.net InetAddress DatagramPacket DatagramSocket)
           (org.apache.commons.validator.routines InetAddressValidator))
  (:gen-class))

(defn- ip-to-inetaddress [ip]
  (try
    (. InetAddress getByName ip)
    (catch Exception _
      (print "Error trying to parse IP address:" ip "/ Please enter a valid IP."))))

(def cli-options
  [["-s" "--server IP:Port" "listen to IP:Port for incoming UDP packets"
    :default "127.0.0.1:49162"
    :parse-fn #(let [str (string/split (string/trim %) #":")]
                 {:ip (ip-to-inetaddress (nth str 0))
                  :port (Integer/parseInt (nth str 1))})
    :validate [#(< 0 (:port %) 0x10000)
               "Please input valid IP address / Port for -s argument"]]
   ["-t"
    "--target IP1:PORT1,IP2:PORT2,...,IPn:PORTn"
    "forward to IP:Port (as many entries as needed, comma-separated)"
    :parse-fn #(let [stra (string/split (string/trim %) #",")
                     strb (map (fn [x] (string/split x #":")) stra)]
                 (map (fn [y] {:ip (ip-to-inetaddress (nth y 0))
                               :port (Integer/parseInt (nth y 1))}) strb))
    :validate [#(every? true? (map (fn [x] (< 0 (:port x) 0x10000)) %))
               "Please input valid IP address"]]
;;   ["-l" "--logfile <filename>" "log output to filename (default:/var/log/udpfork.log)"]
;;   ["-d" "--debug" "Enable debug mode in logfile"]
   ["-h" "--help" "this page"]])

(defn- create-udp-packet [data ip port]
  (let [payload (.getData data)
        len     (.getLength data)]
  (DatagramPacket. payload len ip port)))

(defn- start-udp-server [ip port]
  (try
    (DatagramSocket. port ip)
    (catch Exception _
      (print "Could not open server socket on IP:" (.toString ip)
             ":" (.toString port)))))

(defn- stop-udp-server [s]
  (.close s))

(defn- empty-packet [n] 
  (DatagramPacket. (byte-array n) n))

(defn- dispatch-packet [data socket target]
  (let [{ip   :ip
         port :port} target]
    (.send socket (create-udp-packet data ip port))))

(defn -main [& args]
  (let [arguments        (:options (parse-opts args cli-options))
        {svr-ip   :ip 
         svr-port :port} (:server arguments)
        targets          (:target arguments)
        svr-socket       (start-udp-server svr-ip svr-port)
        recv-packet      (empty-packet 1500)] ;; find out interface mtu?
        (while true
          (.receive svr-socket recv-packet)
          (doall (map (partial dispatch-packet recv-packet svr-socket) targets)))))
