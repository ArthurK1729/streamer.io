package model

case class SourceInfo(sourceName: String,
                      sourceURL: String,
                      webSocket: Boolean, // Unnecessary
                      jsonSelectionThing: String,
                      pollingFrequencySeconds: String
                     )
