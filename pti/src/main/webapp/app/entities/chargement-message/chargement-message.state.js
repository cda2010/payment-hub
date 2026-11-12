(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('chargement-message', {
            parent: 'entity',
            url: '/chargement-message?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementMessage.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-message/chargement-messages.html',
                    controller: 'ChargementMessageController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementMessage');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
            .state('chargement-message-valide', {
                parent: 'entity',
                url: '/chargement-message-valide',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'ptiApp.chargementMessage.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/chargement-message/chargement-messages.html',
                        controller: 'ChargementMessageController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('chargementMessage');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
        .state('chargement-message-detail', {
            parent: 'chargement-message',
            url: '/chargement-message/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.chargementMessage.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/chargement-message/chargement-message-detail.html',
                    controller: 'ChargementMessageDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('chargementMessage');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'ChargementMessage', function($stateParams, ChargementMessage) {
                    return ChargementMessage.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'chargement-message',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('chargement-message-detail.edit', {
            parent: 'chargement-message-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-message/chargement-message-dialog.html',
                    controller: 'ChargementMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ChargementMessage', function(ChargementMessage) {
                            return ChargementMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('chargement-message.new', {
            parent: 'chargement-message',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-message/chargement-message-dialog.html',
                    controller: 'ChargementMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nom_fichier: null,
                                date_chargement: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('chargement-message', null, { reload: 'chargement-message' });
                }, function() {
                    $state.go('chargement-message');
                });
            }]
        })
        .state('chargement-message.edit', {
            parent: 'chargement-message',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-message/chargement-message-dialog.html',
                    controller: 'ChargementMessageDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['ChargementMessage', function(ChargementMessage) {
                            return ChargementMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('chargement-message', null, { reload: 'chargement-message' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('chargement-message.delete', {
            parent: 'chargement-message',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/chargement-message/chargement-message-delete-dialog.html',
                    controller: 'ChargementMessageDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['ChargementMessage', function(ChargementMessage) {
                            return ChargementMessage.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('chargement-message', null, { reload: 'chargement-message' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
