'use strict';

describe('Controller Tests', function() {

    describe('FournisseurService Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockFournisseurService, MockTraitement, MockApi;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockFournisseurService = jasmine.createSpy('MockFournisseurService');
            MockTraitement = jasmine.createSpy('MockTraitement');
            MockApi = jasmine.createSpy('MockApi');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'FournisseurService': MockFournisseurService,
                'Traitement': MockTraitement,
                'Api': MockApi
            };
            createController = function() {
                $injector.get('$controller')("FournisseurServiceDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:fournisseurServiceUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
