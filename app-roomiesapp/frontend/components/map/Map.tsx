'use client';

import type { Map as MapboxMap, Marker } from "mapbox-gl";
import { memo, useEffect, useRef } from "react";
import { createRoot, Root } from "react-dom/client";
import MarkerRole from "./Marker";
import PopupRole from "./PopUp";

const DEFAULT_CENTER: [number, number] = [-35.20551753609717, -5.832075313805946];
const DEFAULT_ZOOM = 12;

interface Local {
    id: number;
    name: string;
    time: string;
    latitude: number;
    longitude: number;
    category: string;
}

interface MapProps {
    locais: Local[];
}

const Map = memo(function Map({ locais }: MapProps) {
    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<MapboxMap | null>(null);
    const markersRef = useRef<Marker[]>([]);
    const rootsRef = useRef<Root[]>([]);

    useEffect(() => {
        if (!mapContainerRef.current || mapRef.current) return;

        let isMounted = true;

        const initMap = async () => {
            const mapboxglModule = await import("mapbox-gl");
            const mapboxgl = mapboxglModule.default || mapboxglModule;

            if (!isMounted) return;

            const token = process.env.NEXT_PUBLIC_MAPBOX_TOKEN;
            if (!token) {
                console.error("NEXT_PUBLIC_MAPBOX_TOKEN não está definido.");
                return;
            }

            mapboxgl.accessToken = token;

            const map = new mapboxgl.Map({
                container: mapContainerRef.current!,
                style: "mapbox://styles/mapbox/streets-v12",
                center: DEFAULT_CENTER,
                zoom: DEFAULT_ZOOM,
            });

            map.addControl(new mapboxgl.NavigationControl(), "top-right");
            mapRef.current = map;
        };

        initMap();

        return () => {
            isMounted = false;
            if (mapRef.current) {
                mapRef.current.remove();
                mapRef.current = null;
            }
        };
    }, []);

    useEffect(() => {
        if (!mapRef.current) return;

        const map = mapRef.current;

        markersRef.current.forEach((marker) => marker.remove());
        markersRef.current = [];

        const oldRoots = rootsRef.current;
        setTimeout(() => {
            oldRoots.forEach((root) => root.unmount());
        }, 0);
        
        rootsRef.current = []; 

        if (locais.length === 0) {
            map.flyTo({ center: DEFAULT_CENTER, zoom: DEFAULT_ZOOM, duration: 1000 });
            return;
        }

        import("mapbox-gl").then((mapboxglModule) => {
            const mapboxgl = mapboxglModule.default || mapboxglModule;
            const bounds = new mapboxgl.LngLatBounds();
            const newRoots: Root[] = [];

            locais.forEach((local) => {
                const markerContainer = document.createElement("div");
                const markerRoot = createRoot(markerContainer);
                markerRoot.render(<MarkerRole order={local.id + 1} />);
                newRoots.push(markerRoot);
                
                const popupContainer = document.createElement("div");
                const popupRoot = createRoot(popupContainer);
                const popup = new mapboxgl.Popup({ offset: 25, closeButton: false }).setDOMContent(popupContainer);
                popupRoot.render(
                    <PopupRole 
                    name={local.name} 
                    category={local.category} 
                    onClose={() => popup.remove()}
                    />
                );
                newRoots.push(popupRoot);
                
                const marker = new mapboxgl.Marker(markerContainer)
                    .setLngLat([local.longitude, local.latitude])
                    .setPopup(popup)
                    .addTo(map);

                markersRef.current.push(marker);
                bounds.extend([local.longitude, local.latitude]);
            });

            rootsRef.current = newRoots;

            map.fitBounds(bounds, {
                padding: window.innerWidth < 768 ? 20 : 60,
                maxZoom: 15,
                duration: 1000,
            });
        });
    }, [locais]);

    return (
        <div className="flex h-full w-full items-stretch">
            <div ref={mapContainerRef} className="h-[calc(100vh-4rem)] w-full" />
        </div>
    );
});

export default Map;