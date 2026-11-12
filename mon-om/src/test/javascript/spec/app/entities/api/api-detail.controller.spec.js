'use strict';

describe('Controller Tests', function() {

    describe('Api Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockApi, MockServiceApi, MockFournisseurService;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockApi = jasmine.createSpy('MockApi');
            MockServiceApi = jasmine.createSpy('MockServiceApi');
            MockFournisseurService = jasmine.createSpy('MockFournisseurService');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Api': MockApi,
                'ServiceApi': MockServiceApi,
                'FournisseurService': MockFournisseurService
            };
            createController = function() {
                $injector.get('$controller')("ApiDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:apiUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
