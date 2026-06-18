import { EventStop, PlaceDetails, PlaceSuggestion } from "@/app/types";
import Map from "@/components/map/Map";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ArrowLeft, ArrowRight, ChevronDown, ChevronUp, Loader2, MapPin, Search, Trash2 } from "lucide-react";
import React, { useEffect, useState } from "react";

interface Step2MapProps {
  stops: EventStop[];
  setStops: React.Dispatch<React.SetStateAction<EventStop[]>>;
  onBack: () => void;
  onNext: () => void;
}

export function Step2Map({ stops, setStops, onBack, onNext }: Step2MapProps) {
  const [searchTerm, setSearchTerm] = useState("");
  const [suggestions, setSuggestions] = useState<PlaceSuggestion[]>([]);
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [isAdding, setIsAdding] = useState<boolean>(false);

  const moveStop = (index: number, direction: "up" | "down") => {
    if (direction === "up" && index === 0) return;
    if (direction === "down" && index === stops.length - 1) return;

    const newStops = [...stops];
    const targetIndex = direction === "up" ? index - 1 : index + 1;
    [newStops[index], newStops[targetIndex]] = [newStops[targetIndex], newStops[index]];
    
    setStops(newStops);
  };

  const removeStop = (id: string) => {
    setStops(stops.filter(stop => stop.id !== id));
  };

  const locaisForMap = stops.map((stop, index) => ({
    id: index,
    name: stop.name,
    time: stop.time || "Horário a definir",
    latitude: stop.latitude,
    longitude: stop.longitude,
    category: stop.category || "default"
  }));

  useEffect(() => {
    const fetchAutocomplete = async () => {
      if (!searchTerm.trim()) {
        setSuggestions([]);
        return;
      }

      setIsSearching(true);
      try {
        const res = await fetch(`/api/places/autocomplete?input=${encodeURIComponent(searchTerm)}`);
        const data = await res.json();
        setSuggestions(data.suggestions || []);
      } catch (error) {
        console.error("Erro ao buscar locais:", error);
      } finally {
        setIsSearching(false);
      }
    };

    const timerId = setTimeout(() => {
      fetchAutocomplete();
    }, 400);

    return () => clearTimeout(timerId);
  }, [searchTerm]);

  const handleSelectPlace = async (placeId: string) => {
    setIsAdding(true);
    setSearchTerm("");
    setSuggestions([]);

    try {
      const res = await fetch(`/api/places/details?placeId=${placeId}`);
      const details: PlaceDetails = await res.json();

      if (details.location) {
        const newStop: EventStop = {
          id: details.id,
          name: details.displayName?.text || "Local sem nome",
          latitude: details.location.latitude,
          longitude: details.location.longitude,
          category: details.primaryTypeDisplayName?.text || "Geral",
          time: "Horário a definir"
        };
        setStops([...stops, newStop]);
      }
    } catch (error) {
      console.error("Erro ao buscar detalhes do local:", error);
    } finally {
      setIsAdding(false);
    }
  };

  return (
    <div className="w-full h-full flex flex-col md:flex-row animate-in fade-in duration-500">
      
      <div className="w-full md:w-[420px] h-full flex flex-col bg-white border-r border-gray-200 z-10 shadow-[4px_0_24px_rgba(0,0,0,0.05)]">
        
        <div className="p-6 border-b border-gray-100 bg-white shrink-0 z-30">
          <h2 className="text-2xl font-bold mb-1 text-gray-900">Defina a Rota</h2>
          <p className="text-gray-500 mb-6 text-sm">Busque e ordene as paradas do rolê.</p>
          
          <div className="relative w-full">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gg-cyan pointer-events-none z-10" />
            
            <Input
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Ex: bares, restaurantes..."
              className="w-full pl-12 pr-14 py-7 bg-white border-gray-200 focus-visible:ring-2 focus-visible:ring-gg-cyan focus-visible:border-gg-cyan rounded-xl text-base shadow-inner transition-all"
            />

            {(isSearching || isAdding) && (
              <div className="absolute right-4 top-1/2 -translate-y-1/2">
                <Loader2 className="w-5 h-5 text-gg-cyan animate-spin" />
              </div>
            )}
            {suggestions.length > 0 && (
              <div className="absolute top-[calc(100%+8px)] left-0 right-0 bg-white border border-gray-200 shadow-[0_20px_40px_rgba(0,0,0,0.1)] rounded-xl overflow-hidden z-100">
                <ul className="max-h-64 overflow-y-auto divide-y divide-gray-100">
                  {suggestions.map((suggestion) => (
                    <li key={suggestion.placePrediction.placeId}>
                      <button
                        onClick={() => handleSelectPlace(suggestion.placePrediction.placeId)}
                        className="w-full text-left px-4 py-3 hover:bg-gray-50 focus:bg-gray-50 outline-none transition-colors flex items-start gap-3 group"
                      >
                        <MapPin className="w-5 h-5 text-gray-400 group-hover:text-gg-cyan shrink-0 mt-0.5 transition-colors" />
                        
                        <div className="flex flex-col min-w-0">
                          <span className="text-sm font-bold text-gray-900 truncate">
                            {suggestion.placePrediction.structuredFormat.mainText.text}
                          </span>
                          {suggestion.placePrediction.structuredFormat.secondaryText?.text && (
                            <span className="text-xs text-gray-500 truncate mt-0.5">
                              {suggestion.placePrediction.structuredFormat.secondaryText.text}
                            </span>
                          )}
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>

        <div className="flex-1 p-4 space-y-3 bg-white overflow-y-auto">
          {stops.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-gray-400">
              <MapPin className="w-12 h-12 mb-2 opacity-30" />
              <p className="text-sm font-medium">Nenhuma parada adicionada.</p>
            </div>
          ) : (
            stops.map((stop, index) => (
              <div key={stop.id} className="flex items-center gap-4 p-4 bg-white border border-gray-200 rounded-2xl shadow-sm hover:border-gg-cyan/40 transition-all group">
                
                <div className="flex flex-col gap-1">
                  <button 
                    onClick={() => moveStop(index, "up")} 
                    disabled={index === 0} 
                    className="text-gray-300 hover:text-gg-cyan disabled:opacity-10 transition-colors"
                  >
                    <ChevronUp className="w-5 h-5" />
                  </button>
                  <button 
                    onClick={() => moveStop(index, "down")} 
                    disabled={index === stops.length - 1} 
                    className="text-gray-300 hover:text-gg-cyan disabled:opacity-10 transition-colors"
                  >
                    <ChevronDown className="w-5 h-5" />
                  </button>
                </div>
                
                <div className="shrink-0 w-8 h-8 bg-[#cc241a] text-white rounded-full flex items-center justify-center font-bold text-sm shadow-sm">
                  {index + 1}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="font-bold text-gray-900 leading-tight truncate">
                    {stop.name}
                  </div>
                  <div className="text-xs text-gray-500 mt-0.5">
                    {stop.category || "Categoria não definida"}
                  </div>
                </div>

                <button 
                  onClick={() => removeStop(stop.id)}
                  className="p-2 text-gray-300 hover:text-gg-cyan transition-colors"
                >
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>
            ))
          )}
        </div>

        <div className="shrink-0 mt-auto z-20 p-6 border-t border-gray-200 bg-white flex gap-3 shadow-[0_-10px_20px_rgba(0,0,0,0.03)]">
          <Button 
            variant="outline"
            onClick={onBack}
            className="px-4 py-6 text-gray-600 border-gray-200 hover:bg-gray-50 rounded-xl font-bold"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <Button
            onClick={onNext}
            disabled={stops.length === 0}
            className="flex-1 py-6 bg-[#cc241a] hover:bg-[#a81d15] disabled:bg-gray-300 text-white font-bold rounded-xl shadow-md transition-all text-lg"
          >
            Finalizar <ArrowRight className="w-5 h-5 ml-2" />
          </Button>
        </div>

      </div>

      <div className="flex-1 bg-gray-200 relative h-[50vh] md:h-full">
        <Map locais={locaisForMap} />
      </div>

    </div>
  );
}