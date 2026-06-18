import Map from "@/components/map/Map";

// Mock de dados para teste. Depois vamos buscar isso da API.
const mockLocais = [
  {
    id: 1,
    name: "Esquenta Universitário UFRN",
    latitude: -5.8324, 
    longitude: -35.2054,
    category: "bar",
    time: "20h00"
  },
  {
    id: 2,
    name: "Museu de Arte Contemporânea",
    latitude: -5.7808,
    longitude: -35.2023,
    category: "cultural",
    time: "17h00"
  },
  {
    id: 3,
    name: "Pizzaria do Zé",
    latitude: -5.7891,
    longitude: -35.1972,
    category: "restaurante",
    time: "19h30"
  },
  {
    id: 4,
    name: "Balada de Sexta",
    latitude: -5.8795,
    longitude: -35.1768,
    category: "balada",
    time: "22h00"
  }
];

export default function MapPage() {
  return (
    <main className="flex min-h-screen flex-col">
      <header className="p-4 text-xl font-semibold">Mapa</header>
      <section className="flex-1">
        <Map locais={mockLocais} />
      </section>
    </main>
  );
}
