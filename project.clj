(defproject packetfork "0.1.0-SNAPSHOT"
  :description "listen on IP:Port, accept packet and relay it to destination(s)"
  :url ""
  :main packetfork.core
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [commons-validator/commons-validator "1.7"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :user
             {:dependencies [[hashp "0.2.0"]]
              :injections [(require 'hashp.core)]}}
  :repl-options {:init-ns packetfork.core}
  :global-vars {;;*warn-on-reflection* true
                *assert* true})
