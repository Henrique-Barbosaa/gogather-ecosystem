import { LoginForm } from "@/components/forms/LoginForm"

export default function LoginPage() {
  return (
    <div className="bg-roomies-gradient flex justify-center items-center w-screen h-screen">
      <div className="p-10 rounded-2xl bg-ra-blue-extralight shadow-2xl w-200 border border-dotted border-ra-blue-extradark h-fit flex flex-col">
        <h1 className="text-7xl font-bold text-center text
          bg-linear-to-r from-ra-purple-light to-ra-green-light
          bg-clip-text text-transparent mb-10">
          Bem vindo de volta!
        </h1>
        <LoginForm/>
      </div>
    </div>
  )
}
