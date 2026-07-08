import Navbar from '@/components/Navbar';
import Sidebar from '@/components/Sidebar';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-[#fbf1c7] font-sans">


      <div className="flex">
        <Sidebar />

        {/* area de conteúdo principal onde as páginas vao renderizadas */}
        <main className='ml-14 w-full'>
          {children}
        </main>
      </div>
    </div>
  );
}
