(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['RequestUtils', '$sessionStorage', '$localStorage', 'Principal', '$rootScope', '$state', '$timeout', 'Auth', '$http', 'Notification'];

    function LoginController(RequestUtils, $sessionStorage, $localStorage, Principal, $rootScope, $state, $timeout, Auth, $http, Notification) {
        var vm = this;

        vm.authenticationError = false;
        vm.cancel = cancel;
        vm.credentials = {};
        vm.login = login;
        vm.password = null;
        vm.register = register;
        vm.rememberMe = true;
        vm.requestResetPassword = requestResetPassword;
        vm.username = null;
        var signedMessage = null;
        vm.sendToLogin = sendToLogin;
        $timeout(function () {
            angular.element('#username').focus();
        });
        getAccount();
        vm.isAuthenticated = Principal.isConnectedStorage;
        function getAccount() {
            Principal.identity().then(function (account) {
                vm.account = account;
                vm.isAuthenticated = Principal.isAuthenticated;
                if (account != null) {
                    $state.go("home");
                }
            });
        }

        function cancel() {
            vm.credentials = {
                username: null,
                password: null,
                rememberMe: true
            };
            vm.authenticationError = false;
        }

        function login(event) {
            RequestUtils.getParametreApp.get({
                "typeParametre": "SYSTEM",
                "nom": "AUTHENTIFICATION_FORTE"
            }, function (data) {
                console.log("verification");
                if (data.valeur === "1") {
                    console.log("Authentification forte active");
                    startVerif();
                } else {
                    vm.sendToLogin(vm.username, vm.username);
                    console.log("Authentification forte desactive");
                }
            }, function () {
                console.log("verification echoue");
                vm.sendToLogin(vm.username, vm.username);
            });
            //event.preventDefault();
            //startVerif();
        }

        function startVerif() {
            var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
            $http.jsonp(url)
                .success(function (data, status, headers, config) {
                    data.login = angular.copy(vm.username);
                    console.log('username',vm.username);
                    console.log('data',data);
                    vm.sendToLogin(JSON.stringify(data), vm.username);
                    $sessionStorage.infos=JSON.stringify(data);
                }).error(function () {
                Notification.warning("Agent local indisponible. Connexion par mot de passe.");
                vm.sendToLogin(vm.username, vm.username);
            });
        }

        function sendToLogin(data, login) {
            Auth.login({
                username: data,
                password: vm.password,
                rememberMe: vm.rememberMe
            }).then(function () {
                vm.authenticationError = false;
                //$uibModalInstance.close();
                if ($state.current.name === 'register' || $state.current.name === 'activate' ||
                    $state.current.name === 'finishReset' || $state.current.name === 'requestReset') {
                    $state.go('home');
                }

                $rootScope.$broadcast('authenticationSuccess');

                
                // previousState was set in the authExpiredInterceptor before being redirected to login modal.
                // since login is successful, go to stored previousState and clear previousState
                if (Auth.getPreviousState()) {
                    var previousState = Auth.getPreviousState();
                    Auth.resetPreviousState();
                    $state.go(previousState.name, previousState.params);
                }
                // $state.go('home');
            }).catch(function () {
                vm.authenticationError = true;
                RequestUtils.UpdateNbrFailure.get({
                    login: login
                }, function () {

                })
                // getAccount();
            });
        }

        function register() {
            //  $uibModalInstance.dismiss('cancel');
            $state.go('register');
        }

        function requestResetPassword() {
            //$uibModalInstance.dismiss('cancel');
            $state.go('requestReset');
        }
    }
})();
