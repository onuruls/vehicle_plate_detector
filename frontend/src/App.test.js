import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

test('renders app without crashing', () => {
  const { container } = render(
    <BrowserRouter>
      <App />
    </BrowserRouter>
  );
  // Verify the app container renders with content
  expect(container.firstChild).toBeTruthy();
});
