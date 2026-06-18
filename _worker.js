export default {
  async fetch(request) {
    const url = new URL(request.url);
    const token = request.headers.get('x-cf-token') || '';

    if (request.method === 'OPTIONS') return new Response(null, { status: 204, headers: corsHeaders() });
    if (!url.pathname.startsWith('/api/cf')) return json({ success: false, error: 'Not found' }, 404);
    if (!token) return json({ success: false, error: 'Missing x-cf-token header' }, 401);

    try {
      const upstreamPath = url.pathname.replace('/api/cf', '') || '/verify';
      const upstream = mapUpstream(request.method, upstreamPath, url.searchParams);
      const upstreamResponse = await fetch(upstream, {
        method: request.method,
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: ['GET', 'HEAD'].includes(request.method) ? undefined : await request.text()
      });
      const text = await upstreamResponse.text();
      return new Response(text, {
        status: upstreamResponse.status,
        headers: {
          ...corsHeaders(),
          'content-type': upstreamResponse.headers.get('content-type') || 'application/json; charset=utf-8'
        }
      });
    } catch (error) {
      return json({ success: false, error: error.message || 'Proxy error' }, 500);
    }
  }
};

function mapUpstream(method, path) {
  const base = 'https://api.cloudflare.com/client/v4';
  if (method === 'POST' && path === '/verify') return `${base}/user/tokens/verify`;
  if (method === 'GET' && path === '/memberships') return `${base}/memberships`;
  if (method === 'GET' && path === '/zones') return `${base}/zones`;
  if (method === 'GET' && /^\/zones\/[^/]+\/dns_records$/.test(path)) return `${base}${path}`;
  if (method === 'GET' && /^\/accounts\/[^/]+\/pages\/projects$/.test(path)) return `${base}${path}`;
  if (method === 'GET' && /^\/accounts\/[^/]+\/pages\/projects\/[^/]+\/deployments$/.test(path)) return `${base}${path}`;
  if (method === 'POST' && path === '/graphql') return `${base}/graphql`;
  return `${base}${path}`;
}

function corsHeaders() {
  return {
    'access-control-allow-origin': '*',
    'access-control-allow-headers': 'content-type, x-cf-token',
    'access-control-allow-methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS'
  };
}

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      ...corsHeaders(),
      'content-type': 'application/json; charset=utf-8'
    }
  });
}
