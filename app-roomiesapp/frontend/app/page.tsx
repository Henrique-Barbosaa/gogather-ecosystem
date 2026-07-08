import Navbar from '@/components/Navbar';
import Image from 'next/image';
import Link from 'next/link';
import { Home as HomeIcon, Wallet, ListTodo, StickyNote, Bot, Receipt, CheckSquare, Heart } from 'lucide-react';
import phoneImg from '../public/phone.svg';
import logoImg from "../public/logo_dark.svg";

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col font-sans">

      {/* hero */}
      <section className="relative min-h-screen flex flex-col bg-roomies-gradient overflow-hidden">
        <div className="absolute inset-0 bg-black/10 z-0"></div>
        <Navbar />

        <div className="flex-1 max-w-7xl w-full mx-auto grid md:grid-cols-2 gap-8 items-center px-6 z-10 mt-20 md:mt-0">
          <div className="flex flex-col items-start text-left">
            <h1 className="text-5xl md:text-6xl font-bold text-white leading-tight mb-6 drop-shadow-lg">
              O fim das brigas.<br />
              <span className="text-[#b0e1ff]">O início da paz na república.</span>
            </h1>
            <p className="text-lg md:text-xl text-white/90 max-w-lg mb-10 drop-shadow-md">
              Esqueça as planilhas e o estresse. Gerencie as contas da casa, divida as tarefas e viva em harmonia com seus colegas de quarto.
            </p>
            <button className="bg-[#8724df] text-white px-8 py-4 rounded-full text-lg font-bold hover:bg-[#6419a8] hover:scale-105 transition-all shadow-xl">
              Organizar minha casa →
            </button>
          </div>

          <div className="relative w-full h-[400px] md:h-[600px] flex justify-center items-center">
            <Image
              src={phoneImg}
              alt="Aplicativo RoomiesApp rodando no celular"
              width={500}
              height={700}
              className="object-contain w-auto h-full drop-shadow-2xl hover:-translate-y-4 transition-transform duration-500"
              priority
            />
          </div>
        </div>
      </section>

      {/* recursos */}
      <section id="recursos" className="py-24 bg-[#e6f5ff] px-6">
        <div className="max-w-7xl mx-auto text-center">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Tudo que você precisa para a <span className="text-[#8724df]">convivência perfeita</span>
          </h2>
          <p className="text-gray-600 mb-16 text-lg">Ferramentas simples e inteligentes para organizar contas, tarefas e a comunicação da casa.</p>

          <div className="grid md:grid-cols-4 gap-6">
            {[
              { title: "Divisão de Contas", desc: "Cadastre luz, água e aluguel. Calculamos exatamente quem deve o que para quem.", icon: Wallet },
              { title: "Escala de Tarefas", desc: "Chega de louça suja. Organize e distribua as tarefas da casa de forma justa.", icon: ListTodo },
              { title: "Mural de Avisos", desc: "Comunicação clara com todos os moradores em um só lugar. Nada se perde.", icon: StickyNote },
              { title: "Chat Inteligente", desc: "Chat integrado que detecta mensagens e automatiza as rotinas da casa.", icon: Bot }
            ].map((recurso, i) => (
              <div key={i} className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-left hover:shadow-md transition-shadow group">
                <div className="w-14 h-14 bg-[#299227]/10 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                  <recurso.icon className="w-7 h-7 text-[#299227]" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-3">{recurso.title}</h3>
                <p className="text-gray-600 leading-relaxed">{recurso.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* como funciona? */}
      <section id="como-funciona" className="py-24 bg-[#299227] px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl font-bold text-white mb-4">Como funciona?</h2>
          <p className="text-white/90 mb-16 text-lg">Em 4 passos simples, do caos à organização total.</p>

          <div className="relative">
            <div className="hidden md:block absolute left-1/2 top-0 bottom-0 w-px bg-white/30 transform -translate-x-1/2 z-0"></div>

            <div className="space-y-12">
              {[
                { step: 1, title: "Crie a Casa", desc: "Dê um nome à sua república ou apartamento e convide os moradores via link.", icon: HomeIcon },
                { step: 2, title: "Adicione as Contas", desc: "Cadastre o aluguel e as despesas fixas. Nós cuidamos da matemática.", icon: Receipt },
                { step: 3, title: "Divida as Tarefas", desc: "Crie a escala de limpeza e deixe todo mundo ciente de suas obrigações.", icon: CheckSquare },
                { step: 4, title: "Viva em Paz", desc: "Acompanhe o painel, pague suas dívidas no Pix e aproveite a tranquilidade.", icon: Heart }
              ].map((item, index) => (
                <div key={item.step} className="flex flex-col md:flex-row items-center gap-8 w-full relative z-10">
                  <div className="md:w-1/2 flex justify-center md:justify-end w-full">
                    {index % 2 === 0 ? (
                      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 w-full max-w-sm text-left hover:border-[#299227]/30 transition-colors">
                        <h4 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                          {item.title} <item.icon className="w-5 h-5 text-[#299227]" />
                        </h4>
                        <p className="text-gray-600 mt-2">{item.desc}</p>
                      </div>
                    ) : (
                      <div className="hidden md:block w-full max-w-sm"></div>
                    )}
                  </div>
                  <div className="w-12 h-12 bg-[#8724df] text-white rounded-full flex items-center justify-center font-bold text-xl shadow-lg border-4 border-white shrink-0">
                    {item.step}
                  </div>
                  <div className="md:w-1/2 flex justify-center md:justify-start w-full">
                    {index % 2 !== 0 ? (
                      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 w-full max-w-sm text-left hover:border-[#299227]/30 transition-colors">
                        <h4 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                          {item.title} <item.icon className="w-5 h-5 text-[#299227]" />
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

      <section className="py-24 bg-roomies-gradient text-center px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-6 drop-shadow-md">
            Pronto para elevar o nível da sua convivência?
          </h2>
          <p className="text-xl text-white/90 mb-10">
            Crie sua casa digital em menos de 1 minuto. Sem enrolação.
          </p>
          <button className="bg-white text-[#8724df] px-8 py-4 rounded-full text-lg font-bold hover:bg-[#e6f5ff] hover:scale-105 transition-all shadow-xl">
            Organizar minha casa — É grátis →
          </button>
        </div>
      </section>

      <footer className="bg-white border-t border-gray-100 py-12 px-6">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-1 opacity-80">
            <Image src={logoImg} alt="RoomiesApp Logo" width={30} height={25} />
            <span className="text-gray-900 text-xl font-bold tracking-tight mt-1">
              roomiesapp
            </span>
          </div>

          <div className="flex gap-6 text-sm text-gray-500 font-medium">
            <Link href="#" className="hover:text-[#8724df] transition-colors">Termos de Uso</Link>
            <Link href="#" className="hover:text-[#8724df] transition-colors">Privacidade</Link>
            <Link href="#" className="hover:text-[#8724df] transition-colors">Contato</Link>
          </div>
        </div>
        <div className="mt-8 text-center text-sm text-gray-400">
          © {new Date().getFullYear()} RoomiesApp. Todos os direitos reservados.
        </div>
      </footer>
    </main>
  );
}