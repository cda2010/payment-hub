(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('app', {
            abstract: true,
            views: {
               // 'navbar@': {
                 //   templateUrl: 'app/layouts/navbar/navbar.html',
                   // controller: 'NavbarController',
                    //controllerAs: 'vm'
                //}
                'root@': {
                    templateUrl: 'app/layouts/navbar/partial.html'
                    
                }
            },
            resolve: {
                authorize: ['Auth',
                    function (Auth) {
                        return Auth.authorize();
                    }
                ]
            }
        })
        
//         .state('logins', {
//            parent: 'app',
//            url: '/logins',
//              data: {
//                authorities: [],
//                pageTitle: 'Api'
//            },
//            views: {
//                '@': {
//                    templateUrl: 'app/components/login/login.html',
//                     controller: 'LoginController',
//                controllerAs: 'vm'
//                }
//            },
//            resolve: {
//               
//            }
//        })
        ;
    }
})();
