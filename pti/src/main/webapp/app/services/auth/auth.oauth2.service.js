(function() {
    /*jshint camelcase: false */
    'use strict';

    angular
        .module('ptiApp')
        .factory('AuthServerProvider', AuthServerProvider);

    AuthServerProvider.$inject = ['$state','$http', '$localStorage', 'Base64','BASE_URL_API','BASE_URL'];

    function AuthServerProvider ($state,$http, $localStorage, Base64,BASE_URL_API,BASE_URL) {
        var service = {
            getToken: getToken,
            login: login,
            logout: logout,
            refreshToken: refreshToken
        };

        return service;

        function getToken () {
            return $localStorage.authenticationToken;
        }


 function refreshToken() {
            var token = $localStorage.authenticationToken;
          
            var data = 'refresh_token=' + encodeURIComponent(token.refresh_token) + '&grant_type=refresh_token&scope=read%20write&' +
            'client_secret=my-secret-token-to-change-in-production&client_id=ptiapp';
        
            return $http.post(BASE_URL+'oauth/token', data, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Accept': 'application/json', 
                    'Authorization': 'Basic ' + Base64.encode('ptiapp' + ':' + 'my-secret-token-to-change-in-production')
                }
            }).success(refreshSucces);
            function refreshSucces (response) {
                console.log("Mon refresh nouveau token à l'entrée "+response.access_token+" Value of expire_in "+response.expires_in+" RefreshTokenNew "+response.refresh_token);
                var expiredAt = new Date();
                expiredAt.setSeconds(expiredAt.getSeconds() + response.expires_in); 
                response.expires_at = expiredAt.getTime();
                $localStorage.authenticationToken = response;
                return response;  
            }
        }

        function login (credentials) {
            var data = 'username=' +  encodeURIComponent(credentials.username) + '&password=' +
                encodeURIComponent(credentials.password) + '&grant_type=password&scope=read%20write&' +
                'client_secret=my-secret-token-to-change-in-production&client_id=ptiapp';
        
        
            return $http.post(BASE_URL+'oauth/token', data, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Accept': 'application/json',
                    'Authorization': 'Basic ' + Base64.encode('ptiapp' + ':' + 'my-secret-token-to-change-in-production')
                }
            }).success(authSucess);

            function authSucess (response) {
                var expiredAt = new Date();
                expiredAt.setSeconds(expiredAt.getSeconds() + response.expires_in);
                response.expires_at = expiredAt.getTime();
                $localStorage.authenticationToken = response;
                return response;
            }
        }

        function logout () {
          console.log("logout ok");
            delete $localStorage.authenticationToken;
            $state.go('login');            

            $http.post(BASE_URL_API+'logout').then(function() {
            });
        }
    }
})();
