(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('liste', {
            parent: 'entity',
            url: '/liste',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.liste.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/liste/listes.html',
                    controller: 'ListeController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('liste');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('liste-detail', {
            parent: 'liste',
            url: '/liste/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.liste.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/liste/liste-detail.html',
                    controller: 'ListeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('liste');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Liste', function($stateParams, Liste) {
                    return Liste.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'liste',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('liste-detail.edit', {
            parent: 'liste-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste/liste-dialog.html',
                    controller: 'ListeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Liste', function(Liste) {
                            return Liste.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('liste.new', {
            parent: 'liste',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste/liste-dialog.html',
                    controller: 'ListeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                options: null,
                                multiple: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('liste', null, { reload: 'liste' });
                }, function() {
                    $state.go('liste');
                });
            }]
        })
        .state('liste.edit', {
            parent: 'liste',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste/liste-dialog.html',
                    controller: 'ListeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['Liste', function(Liste) {
                            return Liste.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('liste', null, { reload: 'liste' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('liste.delete', {
            parent: 'liste',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/liste/liste-delete-dialog.html',
                    controller: 'ListeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Liste', function(Liste) {
                            return Liste.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('liste', null, { reload: 'liste' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
