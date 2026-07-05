/**
 * 统一封装后端接口调用。
 * 后端 Result<T> 统一格式：{ code: 0成功/1失败, message, data }
 * 这里统一拆包，成功返回 data，失败抛出 Error(message) 交给页面 catch 处理。
 */
const Api = (() => {
  async function handle(resp) {
    let body;
    try {
      body = await resp.json();
    } catch (e) {
      throw new Error('接口返回内容无法解析（HTTP ' + resp.status + '）');
    }
    if (!resp.ok || body.code !== 0) {
      throw new Error(body.message || ('请求失败（HTTP ' + resp.status + '）'));
    }
    return body.data;
  }

  function qs(params) {
    if (!params) return '';
    const usp = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') usp.append(k, v);
    });
    const s = usp.toString();
    return s ? ('?' + s) : '';
  }

  return {
    get(url, params) {
      return fetch(url + qs(params)).then(handle);
    },
    post(url, body, params) {
      return fetch(url + qs(params), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body !== undefined ? JSON.stringify(body) : undefined,
      }).then(handle);
    },
    postForm(url, formData, params) {
      return fetch(url + qs(params), { method: 'POST', body: formData }).then(handle);
    },
    put(url, body, params) {
      return fetch(url + qs(params), {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      }).then(handle);
    },
    delete(url, params) {
      return fetch(url + qs(params), { method: 'DELETE' }).then(handle);
    },
  };
})();
