(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('config-publicite', {
            parent: 'entity', 
            url: '/config-publicite?page&sort&search',
            data: {  
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.serviceProfil.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/config-publicite/config-publicite.html',
                    controller: 'ConfigPubliciteController', 
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true 
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('serviceProfil');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('config-publicite.new', {
            parent: 'config-publicite',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/config-publicite/config-publicite-dialog.html',
                    controller: 'ConfigPubliciteDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                pays: null,
                                id: null,
                                frequence:null,
                                dateDebut: null,
                                dateFin:null,
                                urlMinioPublicite:null
                                
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('config-publicite', null, { reload: 'config-publicite' });
                }, function() {
                    $state.go('config-publicite');
                });
            }]
        })
        /* .state('config-publicite.delete', {
            parent: 'config-publicite',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/config-publicite/config-publicite-delete-dialog.html',
                    controller: 'ConfigPubliciteDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ConfigPublicite', function(ConfigPublicite) {
                            return ConfigPublicite.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('config-publicite', null, { reload: 'config-publicite' });
                }, function() {
                    $state.go('^');
                });
            }]
        }); */
    }

})();
