import Navbar from '@/components/Navbar';
import Image from 'next/image';
import Link from 'next/link';
import { Sparkles, Link as LinkIcon, Map, Images, PlusCircle, MapPin, Send, Camera } from 'lucide-react'; // lembrar de instalar lucide-react (npm install lucid-react) e configurar no tsconfig.json para reconhecer os tipos
import phoneImg from '../public/phone.svg';

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col font-sans">

      {/* hero */}
      <section className="relative min-h-screen flex flex-col bg-gogather-gradient overflow-hidden">
        <div className="absolute inset-0 bg-black/10 z-0"></div>
        <Navbar />

        <div className="flex-1 max-w-7xl w-full mx-auto grid md:grid-cols-2 gap-8 items-center px-6 z-10 mt-20 md:mt-0">
          <div className="flex flex-col items-start text-left">
            <h1 className="text-5xl md:text-6xl font-bold text-white leading-tight mb-6 drop-shadow-lg">
              O fim da indecisão.<br />
              <span className="text-[#fbf2c7]">O início do rolê perfeito.</span>
            </h1>
            <p className="text-lg md:text-xl text-white/90 max-w-lg mb-10 drop-shadow-md">
              Esqueça os grupos confusos. Crie o evento, deixe a nossa IA sugerir o roteiro, trace a rota no mapa e convide a galera via link.
            </p>
            <button className="bg-[#cc241a] text-white px-8 py-4 rounded-full text-lg font-bold hover:bg-[#a81d15] hover:scale-105 transition-all shadow-xl">
              Começar agora — É grátis →
            </button>
          </div>

          <div className="relative w-full h-[400px] md:h-[600px] flex justify-center items-center">
            <Image
              src={phoneImg}
              alt="Aplicativo GoGather rodando no celular"
              width={500}
              height={700}
              className="object-contain w-auto h-full drop-shadow-2xl hover:-translate-y-4 transition-transform duration-500"
              priority
            />
          </div>
        </div>
      </section>

      {/* recursos */}
      <section id="recursos" className="py-24 bg-[#fbf2c7] px-6">
        <div className="max-w-7xl mx-auto text-center">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Tudo que você precisa para o <span className="text-[#cc241a]">rolê perfeito</span>
          </h2>
          <p className="text-gray-600 mb-16 text-lg">Ferramentas simples e inteligentes para planejar, convidar e lembrar.</p>

          <div className="grid md:grid-cols-4 gap-6">
            {[
              { title: "Roteiros com IA", desc: "Nossa IA sugere os melhores lugares baseados no gosto do grupo.", icon: Sparkles },
              { title: "Convites em 1 Clique", desc: "Gere um link único. Sem cadastro, sem complicação para os amigos.", icon: LinkIcon },
              { title: "Mapas Interativos", desc: "Veja a rota completa do seu rolê com um visual limpo e intuitivo.", icon: Map },
              { title: "Galeria do Grupo", desc: "Todo mundo sobe as fotos em um único álbum compartilhado.", icon: Images }
            ].map((recurso, i) => (
              <div key={i} className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-left hover:shadow-md transition-shadow group">
                <div className="w-14 h-14 bg-[#458588]/10 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                  <recurso.icon className="w-7 h-7 text-[#458588]" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">{recurso.title}</h3>
                <p className="text-gray-600 leading-relaxed">{recurso.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

            {/* como funciona? */}
      <section id="como-funciona" className="py-24 bg-[#458588] px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl font-bold text-white mb-4">Como funciona?</h2>
          <p className="text-white/90 mb-16 text-lg">Em 4 passos simples, do planejamento à diversão.</p>

          <div className="relative">
            <div className="hidden md:block absolute left-1/2 top-0 bottom-0 w-px bg-gray-300 transform -translate-x-1/2 z-0"></div>

            <div className="space-y-12">
              {[
                { step: 1, title: "Crie o Grupo", desc: "Dê um nome ao rolê, defina a data e chame nossa IA.", icon: PlusCircle },
                { step: 2, title: "Defina as paradas", desc: "Aprove a sugestão da IA ou adicione locais no mapa.", icon: MapPin },
                { step: 3, title: "Mande o convite", desc: "Compartilhe o link e a galera confirma presença na hora.", icon: Send },
                { step: 4, title: "Curta e salve fotos", desc: "Aproveite o momento e guarde as memórias no app.", icon: Camera }
              ].map((item, index) => (
                <div key={item.step} className="flex flex-col md:flex-row items-center gap-8 w-full relative z-10">
                  <div className="md:w-1/2 flex justify-center md:justify-end w-full">
                    {index % 2 === 0 ? (
                      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 w-full max-w-sm text-left hover:border-[#458588]/30 transition-colors">
                        <h4 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                          {item.title} <item.icon className="w-5 h-5 text-[#458588]" />
                        </h4>
                        <p className="text-gray-600 mt-2">{item.desc}</p>
                      </div>
                    ) : (
                      <div className="hidden md:block w-full max-w-sm"></div>
                    )}
                  </div>
                  <div className="w-12 h-12 bg-[#cc241a] text-white rounded-full flex items-center justify-center font-bold text-xl shadow-lg border-4 border-[#f9f7eb] shrink-0">
                    {item.step}
                  </div>
                  <div className="md:w-1/2 flex justify-center md:justify-start w-full">
                    {index % 2 !== 0 ? (
                      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 w-full max-w-sm text-left hover:border-[#458588]/30 transition-colors">
                        <h4 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                          {item.title} <item.icon className="w-5 h-5 text-[#458588]" />
                        </h4>
                        <p className="text-gray-600 mt-2">{item.desc}</p>
                      </div>
                    ) : (
                      <div className="hidden md:block w-full max-w-sm"></div>
                    )}
                  </div>

                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="py-24 bg-gogather-gradient text-center px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-6 drop-shadow-md">
            Pronto para elevar o nível das suas saídas?
          </h2>
          <p className="text-xl text-white/90 mb-10">
            Crie seu primeiro rolê em menos de 1 minuto. Sem enrolação.
          </p>
          <button className="bg-white text-[#cc241a] px-8 py-4 rounded-full text-lg font-bold hover:bg-[#fbf2c7] hover:scale-105 transition-all shadow-xl">
            Começar agora — É grátis →
          </button>
        </div>
      </section>

      <footer className="bg-white border-t border-gray-100 py-12 px-6">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-1 opacity-80">
            <Image src="/logo.svg" alt="GoGather Logo" width={30} height={25} />
            <span className="text-gray-900 text-xl font-bold tracking-tight mt-1">
              gogather
            </span>
          </div>

          <div className="flex gap-6 text-sm text-gray-500 font-medium">
            <Link href="#" className="hover:text-[#cc241a] transition-colors">Termos de Uso</Link>
            <Link href="#" className="hover:text-[#cc241a] transition-colors">Privacidade</Link>
            <Link href="#" className="hover:text-[#cc241a] transition-colors">Contato</Link>
          </div>
        </div>
        <div className="mt-8 text-center text-sm text-gray-400">
          © {new Date().getFullYear()} GoGather. Todos os direitos reservados.
        </div>
      </footer>
    </main>
  );
}
