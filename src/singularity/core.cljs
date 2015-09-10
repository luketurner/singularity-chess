(ns singularity.core
  (:require [reagent.core :as r]
            [singularity.game :as game]
            [singularity.board :as board]
            [singularity.controls :as controls]
            [singularity.style :as style]))

(defn view-root []
  [:div [:style style/css] [board/board] [controls/control-panel]])

(defn init []
  (game/init!)
  (board/init!)
  (r/render-component view-root (.-body js/document)))

(enable-console-print!)
(figwheel.client/start {:on-jsload init})
(init)