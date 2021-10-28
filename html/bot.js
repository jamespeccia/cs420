let address = 'localhost:3000';
document.getElementById('address_url').value = address;

let lastPrice = 0;
function update() {
	fetch('http://' + address + '/getInfo')
		.then((res) => res.json())
		.then((res) => {
			document.getElementById('status').innerText = 'CONNECTED';
			document.getElementById('status').style.color = 'green';

			let algorithm = res.algorithm;
			document.getElementById('price').innerText = `$${res.price}`;
			if (res.price >= res.price) {
				document.getElementById('price').style.color = 'green';
			} else {
				document.getElementById('price').style.color = 'red';
			}
			lastPrice = res.price;

			if(algorithm.name)
				document.getElementById('algorithm').innerText = 'Algorithm: ' + algorithm.name
			else
				document.getElementById('algorithm').innerText = 'Algorithm: Not Selected';
			
			document.getElementById('position').innerText = 'Position: ' + res.position;

			let roi = res.roi;
			document.getElementById('roi').innerText = roi + '%';

			if (roi >= 0) {
				document.getElementById('roi').style.color = 'green';
			} else {
				document.getElementById('roi').style.color = 'red';
			}

			document.getElementById('ping').innerText = 'Ping: ' + res.ping_ms + 'ms';
			document.getElementsByClassName('float-parent')[0].style.visibility = 'visible';

			if (res.algorithm.details_string) {
				document.getElementById('details').innerText = res.algorithm.details_string;
				document.getElementById('details').style.display = 'initial';
			} else {
				document.getElementById('details').style.display = "none"
			}
		})
		.catch((error) => {
			document.getElementById('status').innerText = 'DISCONNECTED';
			document.getElementById('status').style.color = 'red';
			document.getElementById('ping').innerText = 'Ping: 0ms';
		});
}

update();
const interval = setInterval(function () {
	update();
}, 500);

function onConnect(e) {
	e.preventDefault();
	address = document.getElementById('address_url').value;
}

function onSelect(e) {
	e.preventDefault();
	let strategy = e.target.id;

	data = { "algorithm": strategy };
	if (strategy === 'momentum') {
		data.delta = document.getElementById('delta').value;
		data.threshold = document.getElementById('threshold').value;
	}

	console.log(data)
	// else if (strategy == 'meanreversion') {
	// 	data.delta = document.getElementById('length').innerText;
	// 	data.threshold = document.getElementById('percent').innerText;
	// }

	fetch('http://' + address + '/strategy', {
		method: 'POST',
		body: JSON.stringify(data)
	})
		.then((res) => res.text())
		.then((res) => alert(res));
}

let form = document.getElementById('form');
form.addEventListener('submit', onConnect);

form = document.getElementById('momentum');
form.addEventListener('submit', onSelect);

form = document.getElementById('meanreversion');
form.addEventListener('submit', onSelect);
