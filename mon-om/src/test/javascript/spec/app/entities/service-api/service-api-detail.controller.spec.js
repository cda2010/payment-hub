'use strict';

describe('Controller Tests', function() {

    describe('ServiceApi Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockServiceApi, MockApi, MockParametre;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockServiceApi = jasmine.createSpy('MockServiceApi');
            MockApi = jasmine.createSpy('MockApi');
            MockParametre = jasmine.createSpy('MockParametre');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'ServiceApi': MockServiceApi,
                'Api': MockApi,
                'Parametre': MockParametre
            };
            createController = function() {
                $injector.get('$controller')("ServiceApiDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:serviceApiUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
