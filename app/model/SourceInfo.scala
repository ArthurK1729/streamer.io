package model

case class SourceInfo(sourceName: String,
                      sourceURL: String,
                      webSocket: Boolean,
                      jsonSelectionThing: String,
                      pollingFrequency: String
                     )
