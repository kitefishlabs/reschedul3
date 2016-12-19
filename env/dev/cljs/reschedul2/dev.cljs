(ns ^:figwheel-no-load reschedul2.app
  (:require [reschedul2.core :as core]
            [devtools.core :as devtools]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :on-jsload core/mount-components)

(devtools/install!)

(core/init!)
