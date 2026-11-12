(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('authInterceptor', authInterceptor);

    authInterceptor.$inject = ['$rootScope', '$q', '$location', '$localStorage', '$sessionStorage','$injector'];

    function authInterceptor ($rootScope, $q, $location, $localStorage, $sessionStorage,$injector) {
        var service = {
            request: request
        };
  var tokenRefreshInProgress = false;


        return service;

            function request(config) {
            config.headers = config.headers || {};
            var token = $localStorage.authenticationToken || $sessionStorage.authenticationToken;

            if (canRefreshToken() && !tokenRefreshInProgress) {
                tokenRefreshInProgress = true;
                var AuthServerProvider = $injector.get('AuthServerProvider');
                return AuthServerProvider.refreshToken().then(function(response) {
                    config.headers.Authorization = 'Bearer ' + response.access_token;
                    console.log("Mon nouveau token " + response.access_token);
                    tokenRefreshInProgress = false;
                    return config;
                }).catch(function(error) {
                    tokenRefreshInProgress = false;
                    return $q.reject(error);
                });
            } else if (token && token.expires_at && token.expires_at > new Date().getTime()) {
                console.log("My Token Expire Date ============ " + token.expires_at + " Now =========== " + Date.now() + " Mon token s'expire dans ========= " + (token.expires_at - Date.now()));
                config.headers.Authorization = 'Bearer ' + token.access_token;
            }
            return config;
        }

        function canRefreshToken() {
            var token = $localStorage.authenticationToken;
            if (!token || !token.expires_at) {
                return false;
            }
            var timeToEnd = token.expires_at - Date.now();
            // RafraÃ®chit le jeton si la session expire dans une minute ou moins
            return timeToEnd <= 120000 && timeToEnd >= 0;
        }
    }
})();
