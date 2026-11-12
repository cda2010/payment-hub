'use strict';

describe('Controller Tests', function() {

    describe('Traitement Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockTraitement, MockFournisseurService, MockServiceApi, MockTypeTraitement;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockTraitement = jasmine.createSpy('MockTraitement');
            MockFournisseurService = jasmine.createSpy('MockFournisseurService');
            MockServiceApi = jasmine.createSpy('MockServiceApi');
            MockTypeTraitement = jasmine.createSpy('MockTypeTraitement');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Traitement': MockTraitement,
                'FournisseurService': MockFournisseurService,
                'ServiceApi': MockServiceApi,
                'TypeTraitement': MockTypeTraitement
            };
            createController = function() {
                $injector.get('$controller')("TraitementDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:traitementUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
