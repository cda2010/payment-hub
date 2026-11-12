(function() {
    'use strict';
    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('regulation', {
                parent: 'entity',
                url: '/regulation',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'regulation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/regulation/regulation.html',
                        controller: 'RegulationController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            });
    }

})();
