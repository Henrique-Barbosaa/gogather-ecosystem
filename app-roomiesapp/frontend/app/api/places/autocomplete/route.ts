import { NextResponse } from 'next/server';

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const input = searchParams.get('input');

  if (!input) {
    return NextResponse.json({ suggestions: [] });
  }

  const apiKey = process.env.GOOGLE_PLACES_API_KEY;

  if (!apiKey) {
    console.error("ERRO: GOOGLE_PLACES_API_KEY não encontrada no .env.local");
    return NextResponse.json({ suggestions: [] }, { status: 500 });
  }

  try {
    const response = await fetch('https://places.googleapis.com/v1/places:autocomplete', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Goog-Api-Key': apiKey,
      },
      body: JSON.stringify({
        input,
        languageCode: 'pt-BR',
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      console.error("Erro retornado pelo Google Places API:", JSON.stringify(data, null, 2));
      return NextResponse.json({ suggestions: [] }, { status: response.status });
    }

    return NextResponse.json(data);
  } catch (error) {
    console.error("Erro interno ao chamar a API do Google:", error);
    return NextResponse.json({ suggestions: [] }, { status: 500 });
  }
}